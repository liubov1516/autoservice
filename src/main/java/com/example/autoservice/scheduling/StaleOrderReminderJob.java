package com.example.autoservice.scheduling;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

// "Легке" завдання: читає параметри й робить ОДИН виклик сервісу, власної логіки немає.
// Має два тригери (див. QuartzConfig), які перевизначають частину параметрів.
// Стратегія збою: без повторів, фіксуємо помилку й чекаємо наступного запуску за розкладом.
@DisallowConcurrentExecution
public class StaleOrderReminderJob implements Job {

    public static final String OLDER_THAN_HOURS_KEY = "olderThanHours";
    public static final String MIN_INTERVAL_HOURS_KEY = "minIntervalHours";
    public static final String REMINDER_TYPE_KEY = "reminderType";

    private final ScheduledOrderTasksService service;

    public StaleOrderReminderJob(ScheduledOrderTasksService service) {
        this.service = service;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // getMergedJobDataMap(): значення з тригера перекривають значення з JobDetail
        JobDataMap params = context.getMergedJobDataMap();
        int olderThanHours = JobParams.requireInt(params, OLDER_THAN_HOURS_KEY);
        int minIntervalHours = JobParams.requireInt(params, MIN_INTERVAL_HOURS_KEY);
        String reminderType = JobParams.requireString(params, REMINDER_TYPE_KEY);
        try {
            service.remindStaleOrders(olderThanHours, minIntervalHours, reminderType);
        } catch (RuntimeException e) {
            JobFailureHandler.logAndWaitForNextRun(context, e);
        }
    }
}
