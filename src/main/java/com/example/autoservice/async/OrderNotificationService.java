package com.example.autoservice.async;

import com.example.autoservice.dto.RepairOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

// Три повільні побічні ефекти, які раніше блокували б викликаючий код:
// лист клієнту, генерація PDF-рахунку, виклик зовнішнього постачальника.
// Кожен метод виконується в executor'і з AsyncConfig (а не в пулі за замовчуванням).
@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    // Повільна операція 1: надсилання листа клієнту (~800 мс)
    @Async(AsyncConfig.EXECUTOR_BEAN_NAME)
    public CompletableFuture<String> sendOrderCreatedEmail(RepairOrderDto order) {
        simulateSlowWork(800);
        if (order.getClientName() == null || order.getClientName().isBlank()) {
            throw new IllegalStateException("Немає email клієнта для замовлення #" + order.getId());
        }
        String message = "Лист про замовлення #" + order.getId() + " надіслано клієнту " + order.getClientName()
                + " [потік=" + Thread.currentThread().getName() + "]";
        log.info(message);
        return CompletableFuture.completedFuture(message);
    }

    // Повільна операція 2: генерація PDF-рахунку (~800 мс)
    @Async(AsyncConfig.EXECUTOR_BEAN_NAME)
    public CompletableFuture<String> generateInvoicePdf(RepairOrderDto order) {
        simulateSlowWork(800);
        String message = "Рахунок-фактура для замовлення #" + order.getId() + " згенеровано"
                + " [потік=" + Thread.currentThread().getName() + "]";
        log.info(message);
        return CompletableFuture.completedFuture(message);
    }

    // Повільна операція 3: виклик зовнішнього постачальника запчастин.
    // "Запустив і забув" - void @Async, результат нікого не цікавить, лише факт виконання.
    @Async(AsyncConfig.EXECUTOR_BEAN_NAME)
    public void notifyPartsSupplier(RepairOrderDto order) {
        simulateSlowWork(500);
        if (order.getCarModel() == null) {
            throw new IllegalStateException("Не вказано модель авто для замовлення #" + order.getId());
        }
        log.info("Постачальника запчастин повідомлено про замовлення #{}", order.getId());
    }

    private void simulateSlowWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Операцію перервано", e);
        }
    }
}
