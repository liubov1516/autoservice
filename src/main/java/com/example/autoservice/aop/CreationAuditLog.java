package com.example.autoservice.aop;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Реальний журнал аудиту: хто/коли/яке замовлення створив. Зберігається в пам'яті для демонстрації,
// у production тут був би запис у БД чи зовнішню систему аудиту.
@Component
public class CreationAuditLog {

    public record Entry(Long orderId, String clientName, LocalDateTime recordedAt) {
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public void record(Entry entry) {
        entries.add(entry);
    }

    public List<Entry> getEntries() {
        return List.copyOf(entries);
    }
}
