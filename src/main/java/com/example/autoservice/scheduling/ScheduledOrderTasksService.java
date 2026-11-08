package com.example.autoservice.scheduling;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import com.example.autoservice.repository.RepairOrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

// Уся бізнес-логіка запланованих завдань живе тут, а не в класах Job.
// Межа транзакції - на рівні методу сервісу (@Transactional).
// Обидва методи ідемпотентні: повторний запуск одразу після першого нічого не змінює,
// бо опрацьовані замовлення позначаються датою (confirmationSentAt / lastReminderAt).
@Service
public class ScheduledOrderTasksService {

    private final RepairOrderRepository repository;
    private final NotificationSender notificationSender;
    private final Clock clock;

    public ScheduledOrderTasksService(RepairOrderRepository repository,
                                      NotificationSender notificationSender,
                                      Clock clock) {
        this.repository = repository;
        this.notificationSender = notificationSender;
        this.clock = clock;
    }

    // Надсилає листи-підтвердження новим замовленням, яким їх ще не надсилали.
    // Гарантія доставки - "принаймні раз": якщо відправка впаде посеред пачки,
    // транзакція відкотиться і пачка буде повторена цілком.
    @Transactional
    public int sendPendingConfirmations(int batchSize) {
        List<RepairOrder> orders = repository.findByConfirmationSentAtIsNullOrderByIdAsc(PageRequest.of(0, batchSize));
        LocalDateTime now = LocalDateTime.now(clock);
        for (RepairOrder order : orders) {
            notificationSender.sendConfirmation(order);
            order.setConfirmationSentAt(now);
        }
        return orders.size();
    }

    // Нагадує про завислі (не DONE) замовлення, створені давніше за olderThanHours годин,
    // якщо про них не нагадували протягом останніх minIntervalHours годин.
    @Transactional
    public int remindStaleOrders(int olderThanHours, int minIntervalHours, String reminderType) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<RepairOrder> orders = repository.findOrdersNeedingReminder(
                OrderStatus.DONE, now.minusHours(olderThanHours), now.minusHours(minIntervalHours));
        for (RepairOrder order : orders) {
            notificationSender.sendReminder(order, reminderType);
            order.setLastReminderAt(now);
        }
        return orders.size();
    }
}
