package com.example.autoservice.aop;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.repository.RepairOrderRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

// Before advice: бізнес-правило автосервісу - завершене замовлення (DONE) більше не можна редагувати.
// Реальна наскрізна функціональність - контроль цілісності даних, а не приклад з лекції.
@Aspect
@Component
public class LockCompletedOrderAspect {

    private final RepairOrderRepository repository;

    public LockCompletedOrderAspect(RepairOrderRepository repository) {
        this.repository = repository;
    }

    @Before("@annotation(com.example.autoservice.aop.PreventEditingCompletedOrder) && args(id,..)")
    public void preventEditIfCompleted(JoinPoint joinPoint, Long id) {
        repository.findById(id).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.DONE) {
                throw new IllegalStateException("Замовлення #" + id + " вже завершене і не підлягає редагуванню");
            }
        });
    }
}
