package com.example.autoservice.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// Явно налаштований пул потоків для асинхронних операцій - НЕ пул за замовчуванням Spring Boot
// і не SimpleAsyncTaskExecutor (той створює новий потік на кожне завдання без обмежень).
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    public static final String EXECUTOR_BEAN_NAME = "notificationExecutor";

    private final ThreadPoolTaskExecutor executor = buildExecutor();
    private final RecordingAsyncUncaughtExceptionHandler exceptionHandler = new RecordingAsyncUncaughtExceptionHandler();

    @Bean(name = EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor notificationExecutor() {
        return executor;
    }

    @Bean
    public RecordingAsyncUncaughtExceptionHandler recordingAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        // для @Async методів з типом повернення void - сюди падають необроблені винятки,
        // замість того щоб мовчки загубитись
        return exceptionHandler;
    }

    private ThreadPoolTaskExecutor buildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("autoservice-async-");
        executor.initialize();
        return executor;
    }
}
