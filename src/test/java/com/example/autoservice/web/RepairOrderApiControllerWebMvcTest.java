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

// Безпеку тут вимикаємо (addFilters = false). excludeAutoConfiguration прибирає
// автоконфігурацію OAuth2-клієнта, яка інакше під час старту тестового контексту
// намагається піти в мережу на Keycloak-issuer і падає з Connection refused.
@WebMvcTest(controllers = RepairOrderApiController.class,
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class RepairOrderApiControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepairOrderService service;

    @Test
    void createEndpointReturnsCreatedStatusJustLikePostMappingPlusResponseStatus() throws Exception {
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

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void whoAmIEndpointUsesResolvedMechanicNameFromHeaderInResponseBody() throws Exception {
        mockMvc.perform(get("/api/orders/whoami").header("X-Mechanic", "Олег Коваль"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanic").value("Олег Коваль"));
    }

    @Test
    void whoAmIEndpointWithoutHeaderReturnsDefaultValue() throws Exception {
        mockMvc.perform(get("/api/orders/whoami"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanic").value("unknown"));
    }
}
