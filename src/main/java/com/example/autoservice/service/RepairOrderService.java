package com.example.autoservice.service;

import com.example.autoservice.aop.AuditCreation;
import com.example.autoservice.aop.PreventEditingCompletedOrder;
import com.example.autoservice.aop.TrackExecutionTime;
import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import com.example.autoservice.mapper.RepairOrderMapper;
import com.example.autoservice.repository.RepairOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepairOrderService {

    private final RepairOrderRepository repository;
    private final RepairOrderMapper mapper;

    public RepairOrderService(RepairOrderRepository repository, RepairOrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<RepairOrderDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RepairOrderDto findById(Long id) {
        return mapper.toDto(getEntity(id));
    }

    // AOP, аспект 2 (AfterReturning): після успішного створення записує подію в журнал аудиту
    @AuditCreation
    @Transactional
    public RepairOrderDto create(RepairOrderDto dto) {
        if (dto == null) {
            // обробник null
            throw new IllegalArgumentException("Дані замовлення не можуть бути null");
        } else if (dto.getPrice() != null && dto.getPrice().signum() < 0) {
            // гілка else if - обробка невалідного значення
            throw new IllegalArgumentException("Ціна ремонту не може бути від'ємною");
        }
        RepairOrder entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    // AOP, аспект 3 (Before): забороняє редагування вже завершених (DONE) замовлень
    @PreventEditingCompletedOrder
    @Transactional
    public RepairOrderDto update(Long id, RepairOrderDto dto) {
        RepairOrder entity = getEntity(id);
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    // Захист №2: перевірка ролі прямо на методі. Працює незалежно від SecurityFilterChain.
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("total", repository.count());
        for (OrderStatus status : OrderStatus.values()) {
            result.put(status.name(), repository.countByStatus(status));
        }
        return result;
    }

    // AOP, аспект 1 (Around): вимірює час виконання. Викликана ЗЗОВНІ (через проксі-біна) -
    // аспект спрацьовує, запис про тривалість з'являється в ExecutionTimeRecorder.
    @TrackExecutionTime(warnThresholdMs = 100)
    @Transactional(readOnly = true)
    public List<RepairOrderDto> findAllTracked() {
        return findAll();
    }

    // Навмисна демонстрація self-invocation (Лаба №4, обов'язкова частина 2):
    // виклик "this.findAllTracked()" іде напряму на реальний об'єкт, минаючи Spring AOP-проксі,
    // тому ExecutionTimeAspect тут НЕ спрацює. Це свідомий приклад для лабораторної,
    // а не помилка, яку треба виправляти.
    @Transactional(readOnly = true)
    public List<RepairOrderDto> findAllViaSelfInvocation() {
        return this.findAllTracked();
    }

    private RepairOrder getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Замовлення не знайдено"));
    }
}
