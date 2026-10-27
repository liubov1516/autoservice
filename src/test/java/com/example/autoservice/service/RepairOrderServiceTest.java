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

    @Test
    void findAllOrdersExistInRepositoryReturnsMappedDtoList() {
        RepairOrder entity = sampleEntity(1L);
        RepairOrderDto dto = sampleDto(1L);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<RepairOrderDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo("Іван Петренко");
    }

    @Test
    void findAllNoOrdersInRepositoryReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<RepairOrderDto> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdExistingIdReturnsDto() {
        RepairOrder entity = sampleEntity(1L);
        RepairOrderDto dto = sampleDto(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        RepairOrderDto result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdMissingIdThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не знайдено");
    }

    @Test
    void createValidDtoSavesEntityAndReturnsDto() {
        RepairOrderDto inputDto = sampleDto(null);
        RepairOrder entityToSave = sampleEntity(null);
        RepairOrder savedEntity = sampleEntity(1L);
        RepairOrderDto resultDto = sampleDto(1L);

        when(mapper.toEntity(inputDto)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(resultDto);

        RepairOrderDto result = service.create(inputDto);

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getClientName()).isEqualTo("Іван Петренко");
    }

    @Test
    void createNullDtoThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void createNegativePriceThrowsIllegalArgumentException() {
        RepairOrderDto invalidDto = sampleDto(null);
        invalidDto.setPrice(new BigDecimal("-100.00"));

        assertThatThrownBy(() -> service.create(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("від'ємною");

        verifyNoInteractions(repository);
    }

    @Test
    void updateExistingIdUpdatesEntityAndSaves() {
        RepairOrder existing = sampleEntity(1L);
        RepairOrderDto changes = sampleDto(1L);
        changes.setStatus(OrderStatus.DONE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(changes);

        RepairOrderDto result = service.update(1L, changes);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.DONE);
        verify(mapper).updateEntity(changes, existing);
        verify(repository).save(existing);
    }

    @Test
    void updateMissingIdThrowsNotFoundAndDoesNotSave() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, sampleDto(42L)))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deleteExistingIdDeletesEntityFromRepository() {
        RepairOrder existing = sampleEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void deleteMissingIdThrowsNotFoundAndNeverCallsDelete() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(77L))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    void statsOrdersExistReturnsCountsPerStatus() {
        when(repository.count()).thenReturn(5L);
        when(repository.countByStatus(OrderStatus.NEW)).thenReturn(2L);
        when(repository.countByStatus(OrderStatus.IN_PROGRESS)).thenReturn(1L);
        when(repository.countByStatus(OrderStatus.DONE)).thenReturn(2L);

        Map<String, Long> result = service.stats();

        assertThat(result.get("total")).isEqualTo(5L);
        assertThat(result.get("DONE")).isEqualTo(2L);
    }

    @Test
    void statsNoOrdersAtAllReturnsZeroForEveryStatus() {
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(any())).thenReturn(0L);

        Map<String, Long> result = service.stats();

        assertThat(result.get("total")).isEqualTo(0L);
        assertThat(result.get("NEW")).isEqualTo(0L);
        assertThat(result.get("IN_PROGRESS")).isEqualTo(0L);
        assertThat(result.get("DONE")).isEqualTo(0L);
    }

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
