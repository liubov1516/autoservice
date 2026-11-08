package com.example.autoservice.scheduling;

import com.example.autoservice.entity.RepairOrder;

// Відправник сповіщень клієнтам (у лабораторній - лише запис у лог)
public interface NotificationSender {

    void sendConfirmation(RepairOrder order);

    void sendReminder(RepairOrder order, String reminderType);
}
