package com.example.autoservice.repository;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    long countByStatus(OrderStatus status);

    // замовлення, яким ще не надсилали лист-підтвердження (для завдання Quartz)
    List<RepairOrder> findByConfirmationSentAtIsNullOrderByIdAsc(Pageable pageable);

    // завислі замовлення, про які давно не нагадували (для завдання Quartz)
    @Query("select o from RepairOrder o "
            + "where o.status <> :excluded "
            + "and o.createdAt < :createdBefore "
            + "and (o.lastReminderAt is null or o.lastReminderAt < :remindedBefore) "
            + "order by o.id")
    List<RepairOrder> findOrdersNeedingReminder(@Param("excluded") OrderStatus excluded,
                                                @Param("createdBefore") LocalDateTime createdBefore,
                                                @Param("remindedBefore") LocalDateTime remindedBefore);
}
