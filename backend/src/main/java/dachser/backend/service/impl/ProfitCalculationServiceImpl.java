package dachser.backend.service.impl;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.entity.Cost;
import dachser.backend.entity.Income;
import dachser.backend.entity.ProfitCalculation;
import dachser.backend.entity.Shipment;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.mapper.ProfitCalculationMapper;
import dachser.backend.repository.CostRepository;
import dachser.backend.repository.IncomeRepository;
import dachser.backend.repository.ProfitCalculationRepository;
import dachser.backend.repository.ShipmentRepository;
import dachser.backend.service.ProfitCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Default implementation of {@link ProfitCalculationService}.
 * <p>
 * Follows the main flow of the use case: store income, store costs,
 * calculate the totals of everything recorded for the shipment and store the result.
 * The whole flow runs in a single transaction, so a failure leaves no partial data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfitCalculationServiceImpl implements ProfitCalculationService {

    static final String INCOME_DESCRIPTION = "Customer payment";
    static final String COST_DESCRIPTION = "Cost";
    static final String ADDITIONAL_COST_DESCRIPTION = "Additional cost";

    private final ShipmentRepository shipmentRepository;
    private final IncomeRepository incomeRepository;
    private final CostRepository costRepository;
    private final ProfitCalculationRepository profitCalculationRepository;
    private final ProfitCalculationMapper profitCalculationMapper;

    @Override
    @Transactional
    public ProfitCalculationResponse calculate(Long shipmentId, ProfitCalculationRequest request) {
        Shipment shipment = getShipment(shipmentId);

        recordIncome(shipment, request.income());
        recordCost(shipment, COST_DESCRIPTION, request.cost());
        recordCost(shipment, ADDITIONAL_COST_DESCRIPTION, request.additionalCost());

        BigDecimal totalIncome = incomeRepository.sumAmountByShipmentId(shipmentId);
        BigDecimal totalCosts = costRepository.sumAmountByShipmentId(shipmentId);

        ProfitCalculation calculation = new ProfitCalculation();
        calculation.setShipment(shipment);
        calculation.setTotalIncome(totalIncome);
        calculation.setTotalCosts(totalCosts);
        calculation.setProfitOrLoss(totalIncome.subtract(totalCosts));

        ProfitCalculation saved = profitCalculationRepository.save(calculation);
        log.info("Profit calculated for shipment {}: income={}, costs={}, result={}",
                shipment.getShipmentCode(), totalIncome, totalCosts, saved.getProfitOrLoss());

        return profitCalculationMapper.toResponse(saved);
    }

    @Override
    public List<ProfitCalculationResponse> findHistory(Long shipmentId) {
        getShipment(shipmentId);
        return profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(shipmentId).stream()
                .map(profitCalculationMapper::toResponse)
                .toList();
    }

    private Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    /**
     * Stores a new income, ignoring empty or zero values.
     */
    private void recordIncome(Shipment shipment, BigDecimal amount) {
        if (!hasValue(amount)) {
            return;
        }
        Income income = new Income();
        income.setShipment(shipment);
        income.setDescription(INCOME_DESCRIPTION);
        income.setAmount(amount);
        incomeRepository.save(income);
    }

    /**
     * Stores a new cost, ignoring empty or zero values.
     */
    private void recordCost(Shipment shipment, String description, BigDecimal amount) {
        if (!hasValue(amount)) {
            return;
        }
        Cost cost = new Cost();
        cost.setShipment(shipment);
        cost.setDescription(description);
        cost.setAmount(amount);
        costRepository.save(cost);
    }

    private static boolean hasValue(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
}
