package dachser.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result of a profit or loss calculation, as shown in the UI history table.
 *
 * @param id           technical identifier of the calculation
 * @param shipmentId   identifier of the shipment
 * @param shipmentCode business code of the shipment
 * @param totalIncome  income entered in the calculation
 * @param totalCosts   costs entered in the calculation (cost plus additional cost)
 * @param profitOrLoss total income minus total costs (negative means loss)
 * @param calculatedAt date and time of the calculation
 */
public record ProfitCalculationResponse(
        Long id,
        Long shipmentId,
        String shipmentCode,
        BigDecimal totalIncome,
        BigDecimal totalCosts,
        BigDecimal profitOrLoss,
        LocalDateTime calculatedAt
) {
}
