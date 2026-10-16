package com.example.autoservice.service;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import com.example.autoservice.mapper.RepairOrderMapper;
import com.example.autoservice.repository.RepairOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Завдання 2 (Лаба №2): тест з моками (Mockito), без Spring-контексту
@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private RepairOrderMapper mapper;

    @InjectMocks
    private RepairOrderService service;

    // ==================== findAll ====================

    @Test
    void findAllOrdersExistInRepositoryReturnsMappedDtoList() {
        // Arrange
        RepairOrder entity = sampleEntity(1L);
        RepairOrderDto dto = sampleDto(1L);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        List<RepairOrderDto> result = service.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo("Іван Петренко");
    }

    @Test
    void findAllNoOrdersInRepositoryReturnsEmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<RepairOrderDto> result = service.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== findById ====================

    @Test
    void findByIdExistingIdReturnsDto() {
        // Arrange
        RepairOrder entity = sampleEntity(1L);
        RepairOrderDto dto = sampleDto(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        RepairOrderDto result = service.findById(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdMissingIdThrowsNotFound() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не знайдено");
    }

    // ==================== create ====================

    @Test
    void createValidDtoSavesEntityAndReturnsDto() {
        // Arrange
        RepairOrderDto inputDto = sampleDto(null);
        RepairOrder entityToSave = sampleEntity(null);
        RepairOrder savedEntity = sampleEntity(1L);
        RepairOrderDto resultDto = sampleDto(1L);

        when(mapper.toEntity(inputDto)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(resultDto);

        // Act
        RepairOrderDto result = service.create(inputDto);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getClientName()).isEqualTo("Іван Петренко");
    }

    @Test
    void createNullDtoThrowsIllegalArgumentException() {
        // Arrange: dto = null - невалідний вхід (null-обробник у create())

        // Act + Assert
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void createNegativePriceThrowsIllegalArgumentException() {
        // Arrange: ціна менша за нуль - невалідне значення (гілка else if у create())
        RepairOrderDto invalidDto = sampleDto(null);
        invalidDto.setPrice(new BigDecimal("-100.00"));

        // Act + Assert
        assertThatThrownBy(() -> service.create(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("від'ємною");

        verifyNoInteractions(repository);
    }

    // ==================== update ====================

    @Test
    void updateExistingIdUpdatesEntityAndSaves() {
        // Arrange
        RepairOrder existing = sampleEntity(1L);
        RepairOrderDto changes = sampleDto(1L);
        changes.setStatus(OrderStatus.DONE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(changes);

        // Act
        RepairOrderDto result = service.update(1L, changes);

        // Assert
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DONE);
        verify(mapper).updateEntity(changes, existing);
        verify(repository).save(existing);
    }

    @Test
    void updateMissingIdThrowsNotFoundAndDoesNotSave() {
        // Arrange
        when(repository.findById(42L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.update(42L, sampleDto(42L)))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any());
    }

    // ==================== delete ====================

    @Test
    void deleteExistingIdDeletesEntityFromRepository() {
        // Arrange
        RepairOrder existing = sampleEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        // Act
        service.delete(1L);

        // Assert
        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void deleteMissingIdThrowsNotFoundAndNeverCallsDelete() {
        // Arrange
        when(repository.findById(77L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.delete(77L))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).delete(any());
    }

    // ==================== stats ====================

    @Test
    void statsOrdersExistReturnsCountsPerStatus() {
        // Arrange
        when(repository.count()).thenReturn(5L);
        when(repository.countByStatus(OrderStatus.NEW)).thenReturn(2L);
        when(repository.countByStatus(OrderStatus.IN_PROGRESS)).thenReturn(1L);
        when(repository.countByStatus(OrderStatus.DONE)).thenReturn(2L);

        // Act
        Map<String, Long> result = service.stats();

        // Assert
        assertThat(result.get("total")).isEqualTo(5L);
        assertThat(result.get("DONE")).isEqualTo(2L);
    }

    @Test
    void statsNoOrdersAtAllReturnsZeroForEveryStatus() {
        // Arrange
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(any())).thenReturn(0L);

        // Act
        Map<String, Long> result = service.stats();

        // Assert
        assertThat(result.get("total")).isEqualTo(0L);
        assertThat(result.get("NEW")).isEqualTo(0L);
        assertThat(result.get("IN_PROGRESS")).isEqualTo(0L);
        assertThat(result.get("DONE")).isEqualTo(0L);
    }

    // ==================== допоміжні методи ====================

    private RepairOrder sampleEntity(Long id) {
        RepairOrder entity = new RepairOrder();
        entity.setId(id);
        entity.setClientName("Іван Петренко");
        entity.setCarModel("Skoda Octavia");
        entity.setPlateNumber("AA1234BB");
        entity.setDescription("Заміна гальмівних колодок");
        entity.setStatus(OrderStatus.NEW);
        entity.setPrice(new BigDecimal("1500.00"));
        return entity;
    }

    private RepairOrderDto sampleDto(Long id) {
        RepairOrderDto dto = new RepairOrderDto();
        dto.setId(id);
        dto.setClientName("Іван Петренко");
        dto.setCarModel("Skoda Octavia");
        dto.setPlateNumber("AA1234BB");
        dto.setDescription("Заміна гальмівних колодок");
        dto.setStatus(OrderStatus.NEW);
        dto.setPrice(new BigDecimal("1500.00"));
        return dto;
    }
}
