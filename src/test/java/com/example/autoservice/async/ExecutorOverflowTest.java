package com.example.autoservice.async;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Демонстрація переповнення виконавця (Лаба №5): окремий, навмисно крихітний executor
// (без Spring-контексту) - щоб показати сам механізм відхилення завдань.
class ExecutorOverflowTest {

    @Test
    void submittingMoreTasksThanCapacityThrowsTaskRejectedException() {
        // Arrange: пул на 1 потік, черга на 1 завдання - дуже малі навмисно
        ThreadPoolTaskExecutor tinyExecutor = new ThreadPoolTaskExecutor();
        tinyExecutor.setCorePoolSize(1);
        tinyExecutor.setMaxPoolSize(1);
        tinyExecutor.setQueueCapacity(1);
        tinyExecutor.setThreadNamePrefix("overflow-test-");
        tinyExecutor.initialize();

        CountDownLatch blockFirstTask = new CountDownLatch(1);
        try {
            // перше завдання займає єдиний робочий потік і "висить", поки тест не відпустить
            tinyExecutor.execute(() -> {
                try {
                    blockFirstTask.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            // друге завдання йде в чергу (місткість 1) - проходить без помилок
            tinyExecutor.execute(() -> {
            });

            // Act + Assert: третє завдання - і потік зайнятий, і черга вже заповнена -> відхилення
            assertThatThrownBy(() -> tinyExecutor.execute(() -> {
            })).isInstanceOf(TaskRejectedException.class);
        } finally {
            blockFirstTask.countDown();
            tinyExecutor.shutdown();
        }
    }
}
