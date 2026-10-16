package com.example.autoservice.dto;

import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.validation.ValidPlateNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO - те, що бачить клієнт API / форма UI. Entity назовні не віддаємо.
public class RepairOrderDto {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String clientName;

    @NotBlank
    @Size(max = 100)
    private String carModel;

    @NotBlank
    @Size(max = 20)
    @ValidPlateNumber
    private String plateNumber;

    @Size(max = 500)
    private String description;

    @NotNull
    private OrderStatus status = OrderStatus.NEW;

    @PositiveOrZero
    private BigDecimal price;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
