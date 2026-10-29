package com.example.autoservice.controller;

import com.example.autoservice.async.OrderNotificationOrchestrator;
import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.service.RepairOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Безпеку тут вимикаємо (addFilters = false) і виключаємо OAuth2-клієнта (щоб не лізти
// в мережу на Keycloak) - перевіряємо лише поведінку цього ендпоінта.
@WebMvcTest(controllers = OrderNotificationController.class,
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderNotificationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairOrderService orderService;

    @MockBean
    private OrderNotificationOrchestrator orchestrator;

    @Test
    void successfulNotificationReturnsOkWithCombinedResult() throws Exception {
        // Arrange
        RepairOrderDto dto = new RepairOrderDto();
        dto.setId(1L);
        when(orderService.findById(1L)).thenReturn(dto);
        when(orchestrator.notifyAboutNewOrder(dto))
                .thenReturn(CompletableFuture.completedFuture("лист надіслано | рахунок згенеровано"));

        // Act + Assert
        mockMvc.perform(post("/api/orders/1/notify"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("надіслано")));
    }

    @Test
    void rejectedExecutorReturnsServiceUnavailableInsteadOfRandom500() throws Exception {
        // Arrange: виконавець переповнений - кидає TaskRejectedException прямо під час виклику
        RepairOrderDto dto = new RepairOrderDto();
        dto.setId(2L);
        when(orderService.findById(2L)).thenReturn(dto);
        when(orchestrator.notifyAboutNewOrder(dto)).thenThrow(new TaskRejectedException("переповнено"));

        // Act + Assert: зрозуміла відповідь 503, а не випадкова 500
        mockMvc.perform(post("/api/orders/2/notify"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("пізніше")));
    }
}
