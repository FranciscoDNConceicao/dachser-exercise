package dachser.backend.mapper;

import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.entity.ProfitCalculation;
import org.springframework.stereotype.Component;

/**
 * Maps {@link ProfitCalculation} entities to {@link ProfitCalculationResponse} DTOs.
 */
@Component
public class ProfitCalculationMapper {

    /**
     * Converts a calculation entity into its DTO.
     */
    public ProfitCalculationResponse toResponse(ProfitCalculation calculation) {
        return new ProfitCalculationResponse(
                calculation.getId(),
                calculation.getShipment().getId(),
                calculation.getShipment().getShipmentCode(),
                calculation.getTotalIncome(),
                calculation.getTotalCosts(),
                calculation.getProfitOrLoss(),
                calculation.getCalculatedAt()
        );
    }
}
