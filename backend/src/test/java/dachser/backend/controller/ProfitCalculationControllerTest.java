package dachser.backend.controller;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.dto.ShipmentResponse;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.service.ProfitCalculationService;
import dachser.backend.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests: HTTP mapping, validation and error responses (services are mocked).
 */
@WebMvcTest(ProfitCalculationController.class)
class ProfitCalculationControllerTest {

    private static final String CALCULATIONS_URL = "/api/shipments/1/profit-calculations";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;
    @MockBean
    private ProfitCalculationService profitCalculationService;

    @Test
    void findAllShipments_returnsList() throws Exception {
        when(shipmentService.findAll()).thenReturn(List.of(
                new ShipmentResponse(1L, "SHP-0001", "Shipment Porto - Lisbon", "Atlantic Trading Lda")));

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shipmentCode").value("SHP-0001"))
                .andExpect(jsonPath("$[0].customerName").value("Atlantic Trading Lda"));
    }

    @Test
    void findShipmentByCode_returns404WhenNotFound() throws Exception {
        when(shipmentService.findByCode("NOPE")).thenThrow(new ShipmentNotFoundException("NOPE"));

        mockMvc.perform(get("/api/shipments/code/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Shipment not found: NOPE"));
    }

    @Test
    void calculate_returns201WithResult() throws Exception {
        when(profitCalculationService.calculate(eq(1L), any(ProfitCalculationRequest.class)))
                .thenReturn(new ProfitCalculationResponse(5L, 1L, "SHP-0001", new BigDecimal("230.50"),
                        new BigDecimal("200.00"), new BigDecimal("30.50"), LocalDateTime.of(2026, 10, 17, 15, 0)));

        mockMvc.perform(post(CALCULATIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"income\": 230.5, \"cost\": 200, \"additionalCost\": 0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalIncome").value(230.50))
                .andExpect(jsonPath("$.totalCosts").value(200.00))
                .andExpect(jsonPath("$.profitOrLoss").value(30.50));
    }

    @Test
    void calculate_returns400ForNegativeAmount() throws Exception {
        mockMvc.perform(post(CALCULATIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"income\": -5, \"cost\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.startsWith("income:")));

        verifyNoInteractions(profitCalculationService);
    }

    @Test
    void calculate_returns400WhenIncomeIsMissing() throws Exception {
        mockMvc.perform(post(CALCULATIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cost\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.startsWith("income:")));

        verifyNoInteractions(profitCalculationService);
    }

    @Test
    void calculate_returns400ForMalformedJson() throws Exception {
        mockMvc.perform(post(CALCULATIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{oops"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculate_returns404WhenShipmentDoesNotExist() throws Exception {
        when(profitCalculationService.calculate(eq(99L), any(ProfitCalculationRequest.class)))
                .thenThrow(new ShipmentNotFoundException(99L));

        mockMvc.perform(post("/api/shipments/99/profit-calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"income\": 10, \"cost\": 5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findHistory_returnsCalculations() throws Exception {
        when(profitCalculationService.findHistory(1L)).thenReturn(List.of(
                new ProfitCalculationResponse(2L, 1L, "SHP-0001", new BigDecimal("1000.00"),
                        new BigDecimal("200.00"), new BigDecimal("800.00"), LocalDateTime.of(2026, 10, 2, 10, 30))));

        mockMvc.perform(get(CALCULATIONS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].profitOrLoss").value(800.00));
    }

    @Test
    void unexpectedError_returns500WithGenericMessage() throws Exception {
        when(profitCalculationService.findHistory(1L)).thenThrow(new IllegalStateException("database down"));

        mockMvc.perform(get(CALCULATIONS_URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unexpected error, please try again"));
    }
}
