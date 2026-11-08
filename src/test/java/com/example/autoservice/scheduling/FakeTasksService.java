package com.example.autoservice.scheduling;

// Фейковий сервіс для тестів завдань: лише записує, з чим його викликали,
// і за потреби кидає виняток. Без Mockito, без бази даних.
class FakeTasksService extends ScheduledOrderTasksService {

    int confirmationCalls;
    int lastBatchSize;

    int reminderCalls;
    int lastOlderThanHours;
    int lastMinIntervalHours;
    String lastReminderType;

    RuntimeException failure;

    FakeTasksService() {
        super(null, null, null);
    }

    @Override
    public int sendPendingConfirmations(int batchSize) {
        confirmationCalls++;
        lastBatchSize = batchSize;
        if (failure != null) {
            throw failure;
        }
        return 0;
    }

    @Override
    public int remindStaleOrders(int olderThanHours, int minIntervalHours, String reminderType) {
        reminderCalls++;
        lastOlderThanHours = olderThanHours;
        lastMinIntervalHours = minIntervalHours;
        lastReminderType = reminderType;
        if (failure != null) {
            throw failure;
        }
        return 0;
    }
}
