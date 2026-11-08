package com.example.autoservice.scheduling;

import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobExecutionContextImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Тести завдання StaleOrderReminderJob без Spring і без Quartz-планувальника.
class StaleOrderReminderJobTest {

    private final QuartzConfig config = new QuartzConfig();

    @Test
    void dailyTriggerRunsJobWithDefaultParameters() throws Exception {
        // Arrange
        FakeTasksService service = new FakeTasksService();
        StaleOrderReminderJob job = new StaleOrderReminderJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.staleOrderReminderJobDetail(), config.dailyReminderTrigger(), job);

        // Act
        job.execute(context);

        // Assert: один виклик сервісу, параметри - за замовчуванням
        assertThat(service.reminderCalls).isEqualTo(1);
        assertThat(service.lastOlderThanHours).isEqualTo(24);
        assertThat(service.lastMinIntervalHours).isEqualTo(12);
        assertThat(service.lastReminderType).isEqualTo("DAILY");
    }

    @Test
    void urgentTriggerOverridesSomeParametersAndKeepsOthers() throws Exception {
        // Arrange
        FakeTasksService service = new FakeTasksService();
        StaleOrderReminderJob job = new StaleOrderReminderJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.staleOrderReminderJobDetail(), config.urgentReminderTrigger(), job);

        // Act
        job.execute(context);

        // Assert: olderThanHours і reminderType - з тригера, minIntervalHours - за замовчуванням
        assertThat(service.reminderCalls).isEqualTo(1);
        assertThat(service.lastOlderThanHours).isEqualTo(72);
        assertThat(service.lastMinIntervalHours).isEqualTo(12);
        assertThat(service.lastReminderType).isEqualTo("URGENT");
    }

    @Test
    void missingParameterThrowsExplicitErrorAndDoesNotCallService() {
        // Arrange: у завданні задано лише один із трьох параметрів
        FakeTasksService service = new FakeTasksService();
        StaleOrderReminderJob job = new StaleOrderReminderJob(service);
        JobDetail partial = JobBuilder.newJob(StaleOrderReminderJob.class)
                .withIdentity("test", QuartzConfig.GROUP)
                .usingJobData(StaleOrderReminderJob.OLDER_THAN_HOURS_KEY, 24)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob("test", QuartzConfig.GROUP)
                .withIdentity("testTrigger", QuartzConfig.GROUP)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
        JobExecutionContextImpl context = JobContextFactory.create(partial, trigger, job);

        // Act + Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining(StaleOrderReminderJob.MIN_INTERVAL_HOURS_KEY);
        assertThat(service.reminderCalls).isZero();
    }

    @Test
    void failingServiceIsLoggedAndJobWaitsForNextScheduledRun() {
        // Arrange: сервіс кидає виняток
        FakeTasksService service = new FakeTasksService();
        service.failure = new IllegalStateException("база недоступна");
        StaleOrderReminderJob job = new StaleOrderReminderJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.staleOrderReminderJobDetail(), config.dailyReminderTrigger(), job);

        // Act + Assert: завдання не кидає виняток (помилку зафіксовано в лозі),
        // повторів немає - сервіс викликано рівно один раз
        assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();
        assertThat(service.reminderCalls).isEqualTo(1);
    }
}
