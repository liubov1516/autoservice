package com.example.autoservice.repository;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.entity.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    long countByStatus(OrderStatus status);
}
