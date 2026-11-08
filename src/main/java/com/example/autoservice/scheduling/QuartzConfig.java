package com.example.autoservice.scheduling;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

// Усі завдання (JobDetail) і тригери (Trigger) оголошені в одному місці.
// Spring Boot сам реєструє ці біни в планувальнику (Quartz JDBC job store).
@Configuration
public class QuartzConfig {

    public static final String GROUP = "autoservice";

    // Часовий пояс задано явно, щоб cron не залежав від налаштувань сервера.
    // "Europe/Kiev" - сумісна назва сучасного Europe/Kyiv, яка є в усіх версіях Java.
    // Невідомий ідентифікатор Java мовчки замінює на GMT - тому в тесті перевіряємо ID.
    public static final String TIME_ZONE_ID = "Europe/Kiev";

    // ---------- Завдання 1: листи-підтвердження новим замовленням (один тригер) ----------

    @Bean
    public JobDetail orderConfirmationJobDetail() {
        return JobBuilder.newJob(OrderConfirmationJob.class)
                .withIdentity("orderConfirmationJob", GROUP)
                .withDescription("Надсилає листи-підтвердження новим замовленням")
                .usingJobData(OrderConfirmationJob.BATCH_SIZE_KEY, 50)   // значення за замовчуванням
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger orderConfirmationTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(orderConfirmationJobDetail())   // посилання на завдання через JobKey
                .withIdentity("orderConfirmationEveryMinute", GROUP)
                .withDescription("Щохвилини перевіряє нові замовлення")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever()
                        // якщо запуск пропущено - наступний за розкладом, без "доганяння" пропущених
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .build();
    }

    // ---------- Завдання 2: нагадування про завислі замовлення (ДВА тригери) ----------

    @Bean
    public JobDetail staleOrderReminderJobDetail() {
        return JobBuilder.newJob(StaleOrderReminderJob.class)
                .withIdentity("staleOrderReminderJob", GROUP)
                .withDescription("Нагадує про замовлення, які надто довго не завершені")
                // значення за замовчуванням; кожен тригер може перевизначити частину з них
                .usingJobData(StaleOrderReminderJob.OLDER_THAN_HOURS_KEY, 24)
                .usingJobData(StaleOrderReminderJob.MIN_INTERVAL_HOURS_KEY, 12)
                .usingJobData(StaleOrderReminderJob.REMINDER_TYPE_KEY, "DAILY")
                .storeDurably()
                .build();
    }

    // Тригер 1: щодня о 09:00, усі параметри - за замовчуванням із JobDetail
    @Bean
    public Trigger dailyReminderTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(staleOrderReminderJobDetail())
                .withIdentity("dailyReminder", GROUP)
                .withDescription("Щоденне нагадування о 09:00")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 * * ?")
                        .inTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID))
                        // пропущений запуск не повторюємо, чекаємо наступного за cron
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    // Тригер 2: щодня о 15:00, перевизначає olderThanHours і reminderType;
    // minIntervalHours лишається за замовчуванням (12) із JobDetail
    @Bean
    public Trigger urgentReminderTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(staleOrderReminderJobDetail())
                .withIdentity("urgentReminder", GROUP)
                .withDescription("Термінове нагадування о 15:00 про дуже давні замовлення")
                .usingJobData(StaleOrderReminderJob.OLDER_THAN_HOURS_KEY, 72)
                .usingJobData(StaleOrderReminderJob.REMINDER_TYPE_KEY, "URGENT")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 15 * * ?")
                        .inTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}
