package com.example.autoservice.controller;

import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.service.RepairOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Простий UI на Thymeleaf (захищається OIDC-сесією)
@Controller
public class OrderPageController {

    private final RepairOrderService service;

    public OrderPageController(RepairOrderService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String list(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("orders", service.findAll());
        model.addAttribute("username", user.getPreferredUsername());
        model.addAttribute("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .toList());
        return "orders";
    }

    @GetMapping("/orders/new")
    public String newForm(Model model) {
        model.addAttribute("order", new RepairOrderDto());
        model.addAttribute("statuses", OrderStatus.values());
        return "order-form";
    }

    @GetMapping("/orders/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", service.findById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "order-form";
    }

    @PostMapping("/orders")
    public String save(@Valid @ModelAttribute("order") RepairOrderDto dto,
                       BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("statuses", OrderStatus.values());
            return "order-form";
        }
        if (dto.getId() == null) {
            service.create(dto);
        } else {
            service.update(dto.getId(), dto);
        }
        return "redirect:/";
    }

    @PostMapping("/orders/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }
}
