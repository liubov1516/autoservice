package com.example.autoservice.validation;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RepairOrderDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validPlateNumberProducesNoViolations() {
        RepairOrderDto dto = sampleDto("AA1234BB");

        Set<ConstraintViolation<RepairOrderDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidPlateNumberProducesClearViolationMessage() {
        RepairOrderDto dto = sampleDto("12-AB-CD");

        Set<ConstraintViolation<RepairOrderDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("державного номера"));
    }

    private RepairOrderDto sampleDto(String plateNumber) {
        RepairOrderDto dto = new RepairOrderDto();
        dto.setClientName("Іван Петренко");
        dto.setCarModel("Skoda Octavia");
        dto.setPlateNumber(plateNumber);
        dto.setStatus(OrderStatus.NEW);
        dto.setPrice(new BigDecimal("1500.00"));
        return dto;
    }
}
