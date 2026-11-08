package com.example.autoservice.scheduling;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

// Тести класу конфігурації Quartz: без Spring-контексту, без планувальника -
// просто викликаємо методи, що створюють JobDetail і Trigger.
class QuartzConfigTest {

    private final QuartzConfig config = new QuartzConfig();

    // ---------- Завдання й тригери оголошені чітко ----------

    @Test
    void everyTriggerPointsToItsJobByJobKey() {
        assertThat(config.orderConfirmationTrigger().getJobKey())
                .isEqualTo(config.orderConfirmationJobDetail().getKey());
        assertThat(config.dailyReminderTrigger().getJobKey())
                .isEqualTo(config.staleOrderReminderJobDetail().getKey());
        assertThat(config.urgentReminderTrigger().getJobKey())
                .isEqualTo(config.staleOrderReminderJobDetail().getKey());
    }

    @Test
    void everyJobAndTriggerHasExplicitNameAndGroup() {
        List<JobDetail> jobs = List.of(config.orderConfirmationJobDetail(), config.staleOrderReminderJobDetail());
        List<Trigger> triggers = List.of(config.orderConfirmationTrigger(),
                config.dailyReminderTrigger(), config.urgentReminderTrigger());

        for (JobDetail job : jobs) {
            assertThat(job.getKey().getName()).isNotBlank();
            assertThat(job.getKey().getGroup()).isEqualTo(QuartzConfig.GROUP);
        }
        for (Trigger trigger : triggers) {
            assertThat(trigger.getKey().getName()).isNotBlank();
            assertThat(trigger.getKey().getGroup()).isEqualTo(QuartzConfig.GROUP);
        }
    }

    @Test
    void reminderJobHasTwoTriggersWithDifferentNames() {
        assertThat(config.dailyReminderTrigger().getJobKey())
                .isEqualTo(config.urgentReminderTrigger().getJobKey());
        assertThat(config.dailyReminderTrigger().getKey())
                .isNotEqualTo(config.urgentReminderTrigger().getKey());
    }

    // ---------- JobDataMap: тригер перекриває значення завдання ----------

    @Test
    void triggerValuesOverrideJobDefaultsAndMissingKeysKeepDefaults() {
        JobDetail job = config.staleOrderReminderJobDetail();
        Trigger urgent = config.urgentReminderTrigger();

        // getMergedJobDataMap() - саме так Quartz об'єднує дані завдання й тригера
        JobDataMap merged = JobContextFactory.create(job, urgent, new StaleOrderReminderJob(null))
                .getMergedJobDataMap();

        assertThat(merged.getInt(StaleOrderReminderJob.OLDER_THAN_HOURS_KEY)).isEqualTo(72);      // з тригера
        assertThat(merged.getString(StaleOrderReminderJob.REMINDER_TYPE_KEY)).isEqualTo("URGENT"); // з тригера
        assertThat(merged.getInt(StaleOrderReminderJob.MIN_INTERVAL_HOURS_KEY)).isEqualTo(12);     // за замовчуванням
    }

    @Test
    void triggerWithoutOverridesKeepsAllJobDefaults() {
        JobDetail job = config.staleOrderReminderJobDetail();
        Trigger daily = config.dailyReminderTrigger();

        JobDataMap merged = JobContextFactory.create(job, daily, new StaleOrderReminderJob(null))
                .getMergedJobDataMap();

        assertThat(merged.getInt(StaleOrderReminderJob.OLDER_THAN_HOURS_KEY)).isEqualTo(24);
        assertThat(merged.getInt(StaleOrderReminderJob.MIN_INTERVAL_HOURS_KEY)).isEqualTo(12);
        assertThat(merged.getString(StaleOrderReminderJob.REMINDER_TYPE_KEY)).isEqualTo("DAILY");
    }

    // ---------- Паралельне виконання заборонене ----------

    @Test
    void bothJobsDisallowConcurrentExecution() {
        assertThat(config.orderConfirmationJobDetail().isConcurrentExectionDisallowed()).isTrue();
        assertThat(config.staleOrderReminderJobDetail().isConcurrentExectionDisallowed()).isTrue();
    }

    // ---------- Misfire: кожен тригер має явну інструкцію ----------

    @Test
    void simpleTriggerHasExplicitMisfireInstruction() {
        Trigger trigger = config.orderConfirmationTrigger();

        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        assertThat(trigger.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    }

    @Test
    void cronTriggersHaveExplicitMisfireInstruction() {
        assertThat(config.dailyReminderTrigger().getMisfireInstruction())
                .isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        assertThat(config.urgentReminderTrigger().getMisfireInstruction())
                .isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }

    // ---------- Часовий пояс cron-тригерів ----------

    @Test
    void cronTriggersUseExplicitTimeZoneNotServerDefault() {
        CronTrigger daily = (CronTrigger) config.dailyReminderTrigger();
        CronTrigger urgent = (CronTrigger) config.urgentReminderTrigger();

        assertThat(daily.getTimeZone().getID()).isEqualTo(QuartzConfig.TIME_ZONE_ID);
        assertThat(urgent.getTimeZone().getID()).isEqualTo(QuartzConfig.TIME_ZONE_ID);
        // і це справжній пояс, а не тихий запасний варіант GMT
        assertThat(daily.getTimeZone().getID()).isNotEqualTo("GMT");
    }

    @Test
    void unknownTimeZoneIdSilentlyBecomesGmt() {
        // Демонстрація, чому в тесті перевіряємо саме ID: помилка в назві поясу
        // не викликає винятку, Java просто підставляє GMT
        assertThat(TimeZone.getTimeZone("Mars/Olympus").getID()).isEqualTo("GMT");
    }

    // ---------- Значення за замовчуванням у JobDetail ----------

    @Test
    void jobDetailsContainDefaultParameters() {
        assertThat(config.orderConfirmationJobDetail().getJobDataMap().getInt(OrderConfirmationJob.BATCH_SIZE_KEY))
                .isEqualTo(50);
        assertThat(config.staleOrderReminderJobDetail().getJobDataMap().getString(StaleOrderReminderJob.REMINDER_TYPE_KEY))
                .isEqualTo("DAILY");
    }
}
