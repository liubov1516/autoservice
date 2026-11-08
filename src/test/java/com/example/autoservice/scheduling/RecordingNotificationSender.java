package com.example.autoservice.scheduling;

import com.example.autoservice.entity.RepairOrder;

import java.util.ArrayList;
import java.util.List;

// Замість реальних листів запам'ятовує, кому "надіслано", щоб тест міг це перевірити
class RecordingNotificationSender implements NotificationSender {

    final List<Long> confirmations = new ArrayList<>();
    final List<String> reminders = new ArrayList<>();

    @Override
    public void sendConfirmation(RepairOrder order) {
        confirmations.add(order.getId());
    }

    @Override
    public void sendReminder(RepairOrder order, String reminderType) {
        reminders.add(order.getId() + ":" + reminderType);
    }

    void clear() {
        confirmations.clear();
        reminders.clear();
    }
}
