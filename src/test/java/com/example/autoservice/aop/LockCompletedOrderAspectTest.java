package com.example.autoservice.aop;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import com.example.autoservice.repository.RepairOrderRepository;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Тест аспекту (Before advice) з моком репозиторію, без Spring-контексту.
@ExtendWith(MockitoExtension.class)
class LockCompletedOrderAspectTest {

    @Mock
    private RepairOrderRepository repository;

    @InjectMocks
    private LockCompletedOrderAspect aspect;

    @Test
    void preventEditIfCompletedThrowsWhenOrderIsDone() {
        // Arrange
        RepairOrder entity = new RepairOrder();
        entity.setId(1L);
        entity.setStatus(OrderStatus.DONE);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Act + Assert
        assertThatThrownBy(() -> aspect.preventEditIfCompleted(mock(JoinPoint.class), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("завершене");
    }

    @Test
    void preventEditIfCompletedDoesNothingWhenOrderIsNotDone() {
        // Arrange
        RepairOrder entity = new RepairOrder();
        entity.setId(2L);
        entity.setStatus(OrderStatus.IN_PROGRESS);
        when(repository.findById(2L)).thenReturn(Optional.of(entity));

        // Act: не повинно кинути виняток
        aspect.preventEditIfCompleted(mock(JoinPoint.class), 2L);
    }

    @Test
    void preventEditIfCompletedDoesNothingWhenOrderMissing() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act: немає такого замовлення - аспект мовчки пропускає, помилку видасть сам метод update()
        aspect.preventEditIfCompleted(mock(JoinPoint.class), 99L);
    }
}
