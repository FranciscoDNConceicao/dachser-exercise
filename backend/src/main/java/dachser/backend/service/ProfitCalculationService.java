package dachser.backend.service;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;

import java.util.List;

/**
 * Business logic of the "Calculate Profit" use case.
 */
public interface ProfitCalculationService {

    /**
     * Stores the income and costs entered by the user as a new calculation of the shipment,
     * with its profit or loss ({@code income - (cost + additional cost)}).
     *
     * @param shipmentId shipment to calculate
     * @param request    income and costs entered by the user
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
