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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Тести завдання OrderConfirmationJob без Spring і без Quartz-планувальника.
class OrderConfirmationJobTest {

    private final QuartzConfig config = new QuartzConfig();

    // ---------- "Легке" завдання: читає параметри й робить один виклик сервісу ----------

    @Test
    void executeReadsBatchSizeFromJobDataMapAndCallsServiceOnce() throws Exception {
        // Arrange
        FakeTasksService service = new FakeTasksService();
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.orderConfirmationJobDetail(), config.orderConfirmationTrigger(), job);

        // Act
        job.execute(context);

        // Assert
        assertThat(service.confirmationCalls).isEqualTo(1);
        assertThat(service.lastBatchSize).isEqualTo(50);
    }

    @Test
    void triggerCanOverrideBatchSize() throws Exception {
        // Arrange: тригер з власним значенням batchSize
        FakeTasksService service = new FakeTasksService();
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(config.orderConfirmationJobDetail())
                .withIdentity("custom", QuartzConfig.GROUP)
                .usingJobData(OrderConfirmationJob.BATCH_SIZE_KEY, 10)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
        JobExecutionContextImpl context = JobContextFactory.create(config.orderConfirmationJobDetail(), trigger, job);

        // Act
        job.execute(context);

        // Assert
        assertThat(service.lastBatchSize).isEqualTo(10);
    }

    // ---------- Невідповідність параметрів - явна помилка ----------

    @Test
    void missingParameterThrowsExplicitErrorAndDoesNotCallService() {
        // Arrange: у JobDataMap немає жодних параметрів
        FakeTasksService service = new FakeTasksService();
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(jobWithData(null, null), triggerFor(), job);

        // Act + Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("batchSize")
                .hasMessageContaining("відсутній");
        assertThat(service.confirmationCalls).isZero();
    }

    @Test
    void misspelledParameterKeyThrowsExplicitError() {
        // Arrange: ключ з помилкою - "batchSise"
        FakeTasksService service = new FakeTasksService();
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(jobWithData("batchSise", 50), triggerFor(), job);

        // Act + Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("batchSize");
        assertThat(service.confirmationCalls).isZero();
    }

    @Test
    void parameterOfWrongTypeThrowsExplicitError() {
        // Arrange: batchSize задано рядком замість числа
        FakeTasksService service = new FakeTasksService();
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(jobWithData("batchSize", "abc"), triggerFor(), job);

        // Act + Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("цілим числом");
    }

    // ---------- Стратегія збою: обмежені повтори, без нескінченного циклу ----------

    @Test
    void failingServiceIsRetriedLimitedTimesAndThenGivesUp() {
        // Arrange: сервіс завжди кидає виняток
        FakeTasksService service = new FakeTasksService();
        service.failure = new IllegalStateException("пошту недоступно");
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.orderConfirmationJobDetail(), config.orderConfirmationTrigger(), job);

        // Act: повторюємо те, що робить Quartz, коли завдання просить refireImmediately
        int attempts = 0;
        boolean gaveUp = false;
        while (attempts < 10) {   // захист від нескінченного циклу в самому тесті
            attempts++;
            try {
                job.execute(context);
                break;
            } catch (JobExecutionException e) {
                if (e.refireImmediately()) {
                    context.incrementRefireCount();
                } else {
                    gaveUp = true;
                    break;
                }
            }
        }

        // Assert: 1 основна спроба + 2 повтори = 3, далі завдання здається
        assertThat(gaveUp).isTrue();
        assertThat(attempts).isEqualTo(1 + OrderConfirmationJob.MAX_REFIRES);
        assertThat(service.confirmationCalls).isEqualTo(3);
    }

    @Test
    void firstFailureAsksQuartzToRefireImmediately() {
        FakeTasksService service = new FakeTasksService();
        service.failure = new IllegalStateException("пошту недоступно");
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.orderConfirmationJobDetail(), config.orderConfirmationTrigger(), job);

        assertThatExceptionOfType(JobExecutionException.class)
                .isThrownBy(() -> job.execute(context))
                .matches(JobExecutionException::refireImmediately);
    }

    @Test
    void lastAttemptFailureDoesNotAskForRefire() {
        FakeTasksService service = new FakeTasksService();
        service.failure = new IllegalStateException("пошту недоступно");
        OrderConfirmationJob job = new OrderConfirmationJob(service);
        JobExecutionContextImpl context = JobContextFactory.create(
                config.orderConfirmationJobDetail(), config.orderConfirmationTrigger(), job);
        for (int i = 0; i < OrderConfirmationJob.MAX_REFIRES; i++) {
            context.incrementRefireCount();   // імітуємо, що всі повтори вже використано
        }

        assertThatExceptionOfType(JobExecutionException.class)
                .isThrownBy(() -> job.execute(context))
                .matches(e -> !e.refireImmediately());
    }

    // ---------- допоміжні методи ----------

    private JobDetail jobWithData(String key, Object value) {
        JobBuilder builder = JobBuilder.newJob(OrderConfirmationJob.class).withIdentity("test", QuartzConfig.GROUP);
        if (key != null) {
            if (value instanceof Integer) {
                builder.usingJobData(key, (Integer) value);
            } else {
                builder.usingJobData(key, (String) value);
            }
        }
        return builder.build();
    }

    private Trigger triggerFor() {
        return TriggerBuilder.newTrigger()
                .forJob("test", QuartzConfig.GROUP)
                .withIdentity("testTrigger", QuartzConfig.GROUP)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
    }
}
