package dachser.backend.controller;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.dto.ShipmentResponse;
import dachser.backend.service.ProfitCalculationService;
import dachser.backend.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API of the "Calculate Profit" use case.
 * <p>
 * Exposes the shipments (to search and select one) and the profit or loss
 * calculations of each shipment. Errors are handled by
 * {@link GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ProfitCalculationController {

    private final ShipmentService shipmentService;
    private final ProfitCalculationService profitCalculationService;

    /**
     * Lists all shipments.
     */
    @GetMapping
    public List<ShipmentResponse> findAllShipments() {
        return shipmentService.findAll();
    }

    /**
     * Finds a shipment by its business code (e.g. {@code SHP-0001}).
     */
    @GetMapping("/code/{shipmentCode}")
    public ShipmentResponse findShipmentByCode(@PathVariable String shipmentCode) {
        return shipmentService.findByCode(shipmentCode);
    }

    /**
     * Stores the given income and costs as a new calculation of the shipment
     * and returns its profit or loss.
     */
    @PostMapping("/{shipmentId}/profit-calculations")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfitCalculationResponse calculate(@PathVariable Long shipmentId,
                                               @Valid @RequestBody ProfitCalculationRequest request) {
        return profitCalculationService.calculate(shipmentId, request);
    }

    /**
     * Returns the calculation history of the shipment, most recent first.
     */
    @GetMapping("/{shipmentId}/profit-calculations")
    public List<ProfitCalculationResponse> findHistory(@PathVariable Long shipmentId) {
        return profitCalculationService.findHistory(shipmentId);
    }

    /**
     * Deletes a calculation of the shipment (one row of the history),
     * together with the income and costs entered in it.
     */
    @DeleteMapping("/{shipmentId}/profit-calculations/{calculationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCalculation(@PathVariable Long shipmentId, @PathVariable Long calculationId) {
        profitCalculationService.delete(shipmentId, calculationId);
    }
}
