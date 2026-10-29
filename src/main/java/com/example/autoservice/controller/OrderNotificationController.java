package com.example.autoservice.controller;

import com.example.autoservice.async.OrderNotificationOrchestrator;
import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.service.RepairOrderService;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Ендпоінт для демонстрації асинхронної обробки (Лаба №5):
// запускає два незалежні повільні сповіщення, чекає результат з обмеженим тайм-аутом
// і явно обробляє переповнення виконавця замість випадкової помилки 500.
@RestController
@RequestMapping("/api/orders")
public class OrderNotificationController {

    private final RepairOrderService orderService;
    private final OrderNotificationOrchestrator orchestrator;

    public OrderNotificationController(RepairOrderService orderService, OrderNotificationOrchestrator orchestrator) {
        this.orderService = orderService;
        this.orchestrator = orchestrator;
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<String> notifyAboutOrder(@PathVariable Long id) {
        RepairOrderDto order = orderService.findById(id);
        try {
            CompletableFuture<String> future = orchestrator.notifyAboutNewOrder(order);
            String result = future.get(3, TimeUnit.SECONDS); // очікування завжди обмежене в часі
            return ResponseEntity.ok(result);
        } catch (TaskRejectedException e) {
            // явне рішення щодо переповнення виконавця - зрозуміла відповідь, а не випадкова 500
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Сервіс сповіщень перевантажений, спробуйте пізніше");
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Сповіщення ще обробляються, перевірте статус пізніше");
        } catch (ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Не вдалося обробити сповіщення: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Операцію перервано");
        }
    }
}
