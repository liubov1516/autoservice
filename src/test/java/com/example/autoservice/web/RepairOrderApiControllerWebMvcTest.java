package com.example.autoservice.web;

import com.example.autoservice.controller.RepairOrderApiController;
import com.example.autoservice.dto.RepairOrderDto;
import com.example.autoservice.entity.OrderStatus;
import com.example.autoservice.service.RepairOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Безпеку тут вимикаємо (addFilters = false), бо перевіряємо лише MVC-поведінку:
// маршрутизацію, статус-коди й роботу резолвера - а не автентифікацію.
// excludeAutoConfiguration прибирає авто-конфігурацію OAuth2-клієнта: інакше Spring
// під час старту тестового контексту намагається піти по мережі на Keycloak-issuer
// (localhost:8180), якого в unit-тестах не запущено, і падає з Connection refused.
@WebMvcTest(controllers = RepairOrderApiController.class,
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class RepairOrderApiControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairOrderService service;

    // ---------- Тип 2, AC5: композитна анотація дає ту саму поведінку, що й окремі ----------

    @Test
    void createEndpointReturnsCreatedStatusJustLikePostMappingPlusResponseStatus() throws Exception {
        // Arrange
        RepairOrderDto response = new RepairOrderDto();
        response.setId(1L);
        response.setClientName("Іван Петренко");
        response.setCarModel("Skoda Octavia");
        response.setPlateNumber("AA1234BB");
        response.setStatus(OrderStatus.NEW);
        when(service.create(any())).thenReturn(response);

        String requestBody = """
                {
                  "clientName": "Іван Петренко",
                  "carModel": "Skoda Octavia",
                  "plateNumber": "AA1234BB",
                  "status": "NEW"
                }
                """;

        // Act + Assert: той самий результат, який дав би @PostMapping + @ResponseStatus(CREATED) окремо
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ---------- Тип 3, AC8: резолвлене значення реально використовується в тілі методу ----------

    @Test
    void whoAmIEndpointUsesResolvedMechanicNameFromHeaderInResponseBody() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/orders/whoami").header("X-Mechanic", "Олег Коваль"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanic").value("Олег Коваль"));
    }

    @Test
    void whoAmIEndpointWithoutHeaderReturnsDefaultValue() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/orders/whoami"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanic").value("unknown"));
    }
}
