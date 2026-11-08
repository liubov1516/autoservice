package com.example.autoservice.scheduling;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

// "Легке" завдання: читає параметри й робить ОДИН виклик сервісу, власної логіки немає.
// @DisallowConcurrentExecution: якщо попередній запуск ще триває, наступний чекає -
// щоб не надіслати той самий лист двічі одночасно.
// Стратегія збою: до 2 повторів одразу, потім чекаємо наступного запуску.
@DisallowConcurrentExecution
public class OrderConfirmationJob implements Job {

    public static final String BATCH_SIZE_KEY = "batchSize";
    static final int MAX_REFIRES = 2;

    private final ScheduledOrderTasksService service;

    public OrderConfirmationJob(ScheduledOrderTasksService service) {
        this.service = service;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int batchSize = JobParams.requireInt(context.getMergedJobDataMap(), BATCH_SIZE_KEY);
        try {
            service.sendPendingConfirmations(batchSize);
        } catch (RuntimeException e) {
            JobFailureHandler.retryLimited(context, e, MAX_REFIRES);
        }
    }
}
