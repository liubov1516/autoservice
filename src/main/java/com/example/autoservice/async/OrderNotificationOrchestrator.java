package com.example.autoservice.async;

import com.example.autoservice.dto.RepairOrderDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

// Окремий клас-оркестратор: викликає @Async методи OrderNotificationService ЗЗОВНІ
// (через інжектований біт-проксі), а не через "this." всередині самого сервісу.
// Якби ці виклики були всередині OrderNotificationService, вони пройшли б повз
// Spring AOP-проксі (той самий self-invocation, що й у Лабі №4) і виконувались би синхронно.
@Service
public class OrderNotificationOrchestrator {

    private final OrderNotificationService notificationService;

    public OrderNotificationOrchestrator(OrderNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Запускає два незалежні повільні виклики, не чекаючи жодного з них одразу,
    // і об'єднує результати через thenCombine. Збій одного з них не ламає інший.
    public CompletableFuture<String> notifyAboutNewOrder(RepairOrderDto order) {
        CompletableFuture<String> emailFuture = notificationService.sendOrderCreatedEmail(order)
                .exceptionally(ex -> "Лист не надіслано: " + ex.getMessage());
        CompletableFuture<String> pdfFuture = notificationService.generateInvoicePdf(order)
                .exceptionally(ex -> "Рахунок не згенеровано: " + ex.getMessage());

        // fire-and-forget: запускаємо, результат і помилки нікого тут не цікавлять
        notificationService.notifyPartsSupplier(order);

        return emailFuture.thenCombine(pdfFuture, (emailResult, pdfResult) -> emailResult + " | " + pdfResult);
    }
}
