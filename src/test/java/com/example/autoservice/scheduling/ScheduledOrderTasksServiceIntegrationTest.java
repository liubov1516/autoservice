package com.example.autoservice.scheduling;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import com.example.autoservice.repository.RepairOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

// Інтеграційний тест ідемпотентності: справжні JPA-запити на вбудованій H2
// (не Postgres і не Liquibase - схему тут створює Hibernate). Запускається так само,
// як звичайний тест, Docker не потрібен.
@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ScheduledOrderTasksService.class)
class ScheduledOrderTasksServiceIntegrationTest {

    @TestConfiguration
    static class TestBeans {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        RecordingNotificationSender notificationSender() {
            return new RecordingNotificationSender();
        }
    }

    @Autowired
    private ScheduledOrderTasksService service;

    @Autowired
    private RepairOrderRepository repository;

    @Autowired
    private RecordingNotificationSender sender;

    @Autowired
    private Clock clock;

    @BeforeEach
    void clearSender() {
        sender.clear();
    }

    // ---------- Листи-підтвердження ----------

    @Test
    void sendPendingConfirmationsRunTwiceSecondRunChangesNothing() {
        // Arrange
        RepairOrder first = saveOrder(OrderStatus.NEW, LocalDateTime.now(clock));
        RepairOrder second = saveOrder(OrderStatus.NEW, LocalDateTime.now(clock));

        // Act: два запуски поспіль для одних і тих самих даних
        int firstRun = service.sendPendingConfirmations(50);
        int secondRun = service.sendPendingConfirmations(50);

        // Assert: перший запуск опрацював обидва замовлення, другий - нічого
        assertThat(firstRun).isEqualTo(2);
        assertThat(secondRun).isZero();
        assertThat(sender.confirmations).containsExactly(first.getId(), second.getId());
        assertThat(first.getConfirmationSentAt()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void sendPendingConfirmationsRespectsBatchSize() {
        // Arrange
        saveOrder(OrderStatus.NEW, LocalDateTime.now(clock));
        saveOrder(OrderStatus.NEW, LocalDateTime.now(clock));
        saveOrder(OrderStatus.NEW, LocalDateTime.now(clock));

        // Act
        int firstRun = service.sendPendingConfirmations(2);
        int secondRun = service.sendPendingConfirmations(2);
        int thirdRun = service.sendPendingConfirmations(2);

        // Assert: 2 + 1 + 0, кожне замовлення отримало лист рівно один раз
        assertThat(firstRun).isEqualTo(2);
        assertThat(secondRun).isEqualTo(1);
        assertThat(thirdRun).isZero();
        assertThat(sender.confirmations).hasSize(3).doesNotHaveDuplicates();
    }

    // ---------- Нагадування ----------

    @Test
    void remindStaleOrdersRunTwiceSecondRunChangesNothing() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(clock);
        RepairOrder stale = saveOrder(OrderStatus.NEW, now.minusHours(30));         // завислe - нагадати
        saveOrder(OrderStatus.DONE, now.minusHours(30));                            // завершене - ігнорувати
        saveOrder(OrderStatus.NEW, now.minusHours(1));                              // свіже - ігнорувати

        // Act
        int firstRun = service.remindStaleOrders(24, 12, "DAILY");
        int secondRun = service.remindStaleOrders(24, 12, "DAILY");

        // Assert
        assertThat(firstRun).isEqualTo(1);
        assertThat(secondRun).isZero();
        assertThat(sender.reminders).containsExactly(stale.getId() + ":DAILY");
        assertThat(stale.getLastReminderAt()).isEqualTo(now);
    }

    @Test
    void remindStaleOrdersWithLargerThresholdSkipsYoungerOrders() {
        // Arrange: замовлення старе на 30 годин
        saveOrder(OrderStatus.IN_PROGRESS, LocalDateTime.now(clock).minusHours(30));

        // Act: термінове нагадування з порогом 72 години його не стосується
        int urgentRun = service.remindStaleOrders(72, 12, "URGENT");

        // Assert
        assertThat(urgentRun).isZero();
        assertThat(sender.reminders).isEmpty();
    }

    // ---------- допоміжний метод ----------

    private RepairOrder saveOrder(OrderStatus status, LocalDateTime createdAt) {
        RepairOrder order = new RepairOrder();
        order.setClientName("Іван Петренко");
        order.setCarModel("Skoda Octavia");
        order.setPlateNumber("AA1234BB");
        order.setStatus(status);
        order.setPrice(new BigDecimal("1500.00"));
        order.setCreatedAt(createdAt);
        return repository.save(order);
    }
}
