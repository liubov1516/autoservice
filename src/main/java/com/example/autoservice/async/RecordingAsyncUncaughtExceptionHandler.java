package com.example.autoservice.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Обробник помилок для @Async методів з типом повернення void.
// Пише в лог І запам'ятовує винятки - щоб тест міг перевірити, що помилка дійсно зафіксована,
// а не мовчки загублена.
public class RecordingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RecordingAsyncUncaughtExceptionHandler.class);

    private final List<Throwable> handled = new CopyOnWriteArrayList<>();

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Необроблений виняток в асинхронному методі {}: {}", method.getName(), ex.getMessage());
        handled.add(ex);
    }

    public List<Throwable> getHandled() {
        return List.copyOf(handled);
    }
}
