package com.example.autoservice.aop;

import com.example.autoservice.dto.RepairOrderDto;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

// Тест аспекту (AfterReturning advice) НАПРЯМУ, без Spring-контексту.
class AuditCreationAspectTest {

    private final CreationAuditLog auditLog = new CreationAuditLog();
    private final AuditCreationAspect aspect = new AuditCreationAspect(auditLog);

    @Test
    void auditRecordsEntryWhenReturnedValueIsRepairOrderDto() {
        // Arrange
        RepairOrderDto dto = new RepairOrderDto();
        dto.setId(5L);
        dto.setClientName("Ірина Ковальчук");

        // Act
        aspect.audit(mock(JoinPoint.class), dto);

        // Assert
        assertThat(auditLog.getEntries()).hasSize(1);
        assertThat(auditLog.getEntries().get(0).orderId()).isEqualTo(5L);
        assertThat(auditLog.getEntries().get(0).clientName()).isEqualTo("Ірина Ковальчук");
    }

    @Test
    void auditIgnoresReturnedValueOfUnexpectedType() {
        // Act
        aspect.audit(mock(JoinPoint.class), "не DTO");

        // Assert
        assertThat(auditLog.getEntries()).isEmpty();
    }
}
