package com.example.autoservice.controller;

import com.example.autoservice.service.RepairOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Шлях /api/admin/** закритий для не-ADMIN у SecurityFilterChain (Захист №1)
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final RepairOrderService service;

    public AdminApiController(RepairOrderService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return service.stats();
    }
}
