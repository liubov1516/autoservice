package com.example.autoservice.async;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

// Тести реальної асинхронної поведінки (Лаба №5). Тут НЕ мокається виконавець -
// піднято маленький Spring-контекст лише з потрібними бінами (без вебу, без security,
// без мережі), щоб побачити справжній AOP-проксі й реальний пул потоків.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AsyncConfig.class, OrderNotificationService.class, OrderNotificationOrchestrator.class})
class AsyncBehaviorTest {

    @Autowired
    private OrderNotificationService notificationService;

    @Autowired
    private OrderNotificationOrchestrator orchestrator;

    @Autowired
    private RecordingAsyncUncaughtExceptionHandler exceptionHandler;

    @Autowired
    @Qualifier(AsyncConfig.EXECUTOR_BEAN_NAME)
    private ThreadPoolTaskExecutor executor;

    // Контекст спільний для всіх тестів. Перед кожним тестом чекаємо, поки пул звільниться.
    @BeforeEach
    void waitUntilExecutorIsIdle() {
        await().atMost(Duration.ofSeconds(5)).until(() ->
                executor.getThreadPoolExecutor().getTaskCount()
                        == executor.getThreadPoolExecutor().getCompletedTaskCount());
    }

    @Test
    void slowAsyncMethodReturnsControlBeforeCompletion() {
        // Arrange
        RepairOrderDto order = sampleOrder(1L, "Іван Петренко", "Skoda Octavia");

        // Act
        long start = System.currentTimeMillis();
        CompletableFuture<String> future = notificationService.sendOrderCreatedEmail(order);
        long callDuration = System.currentTimeMillis() - start;

        // Assert: виклик повернувся майже миттєво, не чекаючи ~800 мс "надсилання листа"
        assertThat(callDuration).isLessThan(300);
        assertThat(future.join()).contains("надіслано");
    }

    @Test
    void asyncMethodRunsOnConfiguredExecutorNotOnDefaultOne() {
        // Arrange
        RepairOrderDto order = sampleOrder(2L, "Марія Шевченко", "Toyota Corolla");

        // Act
        String result = notificationService.sendOrderCreatedEmail(order).join();

        // Assert: ім'я потоку починається з префіксу нашого executor'а
        assertThat(result).contains("autoservice-async-");
    }

    @Test
    void twoIndependentAsyncCallsRunConcurrentlyAndCombineResults() {
        // Arrange
        RepairOrderDto order = sampleOrder(3L, "Олег Коваль", "VW Golf");

        // Act
        long start = System.currentTimeMillis();
        String result = orchestrator.notifyAboutNewOrder(order).join();
        long totalDuration = System.currentTimeMillis() - start;

        // Assert: обидва результати присутні, і разом це зайняло менше, ніж сума двох "по 800 мс"
        assertThat(result).contains("надіслано").contains("згенеровано");
        assertThat(totalDuration).isLessThan(1400);
    }

    @Test
    void failedAsyncCallDoesNotBreakCombinedResult() {
        // Arrange: порожнє ім'я клієнта -> sendOrderCreatedEmail кине виняток
        RepairOrderDto order = sampleOrder(4L, "", "Renault Megane");

        // Act
        String result = orchestrator.notifyAboutNewOrder(order).join();

        // Assert: виклик не впав, помилку перехоплено через exceptionally(), другий виклик відпрацював
        assertThat(result).contains("Лист не надіслано");
        assertThat(result).contains("згенеровано");
    }

    @Test
    void callerStopsWaitingWhenFutureTakesTooLong() {
        // Arrange
        RepairOrderDto order = sampleOrder(5L, "Клієнт", "Авто");

        // Act
        CompletableFuture<String> future = notificationService.sendOrderCreatedEmail(order); // ~800 мс

        // Assert: чекаємо лише 100 мс - значно менше, ніж реальна тривалість
        assertThatThrownBy(() -> future.get(100, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void voidAsyncMethodFailureIsCapturedByUncaughtExceptionHandlerNotSwallowed() {
        // Arrange: немає моделі авто, notifyPartsSupplier кине виняток
        RepairOrderDto order = sampleOrder(6L, "Петро Гриценко", null);

        // Act
        notificationService.notifyPartsSupplier(order);

        // Assert: помилка не загубилась мовчки, а потрапила в обробник
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertThat(exceptionHandler.getHandled()).isNotEmpty());
        assertThat(exceptionHandler.getHandled().get(0)).isInstanceOf(IllegalStateException.class);
    }

    private RepairOrderDto sampleOrder(Long id, String clientName, String carModel) {
        RepairOrderDto dto = new RepairOrderDto();
        dto.setId(id);
        dto.setClientName(clientName);
        dto.setCarModel(carModel);
        dto.setPlateNumber("AA1234BB");
        dto.setStatus(OrderStatus.NEW);
        dto.setPrice(new BigDecimal("1000.00"));
        return dto;
    }
}
