package com.example.autoservice.scheduling;

import com.example.autoservice.entity.RepairOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Замість реальної пошти просто пишемо в лог, що лист "надіслано"
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void sendConfirmation(RepairOrder order) {
        log.info("Лист-підтвердження для замовлення #{} надіслано клієнту {}", order.getId(), order.getClientName());
    }

    @Override
    public void sendReminder(RepairOrder order, String reminderType) {
        log.info("Нагадування [{}] про замовлення #{} надіслано клієнту {}",
                reminderType, order.getId(), order.getClientName());
    }
}
