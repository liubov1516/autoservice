package com.example.autoservice.aop;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Проста "пам'ять" для тривалостей виконання - реальний артефакт, який можна перевірити в тестах,
// а не лише текст у логах.
@Component
public class ExecutionTimeRecorder {

    private final List<Long> durationsMs = new CopyOnWriteArrayList<>();

    public void record(long durationMs) {
        durationsMs.add(durationMs);
    }

    public int callCount() {
        return durationsMs.size();
    }

    public List<Long> getDurationsMs() {
        return List.copyOf(durationsMs);
    }
}
