package dachser.backend.service;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;

import java.util.List;

/**
 * Business logic of the "Calculate Profit" use case.
 */
public interface ProfitCalculationService {

    /**
     * Records the new income and costs of a shipment, calculates its profit or loss
     * ({@code total income - total costs}) and stores the result.
     *
     * @param shipmentId shipment to calculate
     * @param request    new income and costs entered by the user
     * @return the stored calculation
     * @throws dachser.backend.exception.ShipmentNotFoundException if the shipment does not exist
     */
    ProfitCalculationResponse calculate(Long shipmentId, ProfitCalculationRequest request);

    /**
     * Returns the calculation history of a shipment, most recent first.
     *
     * @throws dachser.backend.exception.ShipmentNotFoundException if the shipment does not exist
     */
    List<ProfitCalculationResponse> findHistory(Long shipmentId);
}
