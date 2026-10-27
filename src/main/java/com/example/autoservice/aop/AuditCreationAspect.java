package com.example.autoservice.aop;

import com.example.autoservice.dto.RepairOrderDto;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// AfterReturning advice: після успішного створення замовлення записує подію в журнал аудиту.
// Реальна наскрізна функціональність - аудит бізнес-дій, а не контроль доступу з лекції.
@Aspect
@Component
public class AuditCreationAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditCreationAspect.class);

    private final CreationAuditLog auditLog;

    public AuditCreationAspect(CreationAuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @AfterReturning(pointcut = "@annotation(AuditCreation)", returning = "result")
    public void audit(JoinPoint joinPoint, Object result) {
        if (result instanceof RepairOrderDto dto) {
            CreationAuditLog.Entry entry = new CreationAuditLog.Entry(dto.getId(), dto.getClientName(), LocalDateTime.now());
            auditLog.record(entry);
            log.info("Аудит створення: замовлення #{} ({})", entry.orderId(), entry.clientName());
        }
    }
}
