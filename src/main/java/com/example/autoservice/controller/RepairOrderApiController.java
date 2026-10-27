package com.example.autoservice.controller;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.service.RepairOrderService;
import com.example.autoservice.web.CurrentMechanic;
import com.example.autoservice.web.PostMappingCreated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST API (захищається JWT)
@RestController
@RequestMapping("/api/orders")
public class RepairOrderApiController {

    private final RepairOrderService service;

    public RepairOrderApiController(RepairOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<RepairOrderDto> getAll() {
        return service.findAll();
    }

    // Точний літеральний шлях має пріоритет над /{id}, тому конфлікту з getOne() немає
    @GetMapping("/whoami")
    public Map<String, String> whoAmI(@CurrentMechanic String mechanicName) {
        return Map.of("mechanic", mechanicName);
    }

    @GetMapping("/{id}")
    public RepairOrderDto getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    // Композитна анотація замінює пару @PostMapping + @ResponseStatus(CREATED)
    @PostMappingCreated
    public RepairOrderDto create(@Valid @RequestBody RepairOrderDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public RepairOrderDto update(@PathVariable Long id, @Valid @RequestBody RepairOrderDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
