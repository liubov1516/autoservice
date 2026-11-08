package com.example.autoservice.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Дві чітко визначені стратегії на випадок збою завдання.
final class JobFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(JobFailureHandler.class);

    private JobFailureHandler() {
    }

    // Стратегія 1: повторити одразу, але не більше maxRefires разів.
    // Після вичерпання спроб - фіксуємо помилку й чекаємо наступного запуску за розкладом.
    // Нескінченного циклу не буває: лічильник getRefireCount() росте з кожною спробою.
    static void retryLimited(JobExecutionContext context, RuntimeException cause, int maxRefires)
            throws JobExecutionException {
        if (context.getRefireCount() < maxRefires) {
            log.warn("Збій завдання {} (спроба {} з {}): {}. Повторюємо.",
                    context.getJobDetail().getKey(), context.getRefireCount() + 1, maxRefires + 1, cause.getMessage());
            throw new JobExecutionException(cause, true);
        }
        log.error("Завдання {} не вдалося після {} спроб: {}. Чекаємо наступного запуску за розкладом.",
                context.getJobDetail().getKey(), maxRefires + 1, cause.getMessage());
        throw new JobExecutionException(cause, false);
    }

    // Стратегія 2: без повторів - лише фіксуємо помилку в лозі й чекаємо наступного запуску.
    static void logAndWaitForNextRun(JobExecutionContext context, RuntimeException cause) {
        log.error("Збій завдання {}: {}. Наступна спроба - за розкладом.",
                context.getJobDetail().getKey(), cause.getMessage());
    }
}
