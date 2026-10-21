package dachser.backend.service.impl;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.entity.Cost;
import dachser.backend.entity.Income;
import dachser.backend.entity.ProfitCalculation;
import dachser.backend.entity.Shipment;
import dachser.backend.exception.ProfitCalculationNotFoundException;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.mapper.ProfitCalculationMapper;
import dachser.backend.repository.ProfitCalculationRepository;
import dachser.backend.repository.ShipmentRepository;
import dachser.backend.service.ProfitCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * Default implementation of {@link ProfitCalculationService}.
 * <p>
 * Follows the main flow of the use case: the income and costs entered by the user are
 * stored with a new calculation of the shipment, and its profit or loss is calculated from them.
 * Each calculation is independent (one row of the history table) and owns its income and costs.
 * Every operation runs in a single transaction, so a failure leaves no partial data.
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
    private final ProfitCalculationRepository profitCalculationRepository;
    private final ProfitCalculationMapper profitCalculationMapper;

    @Override
    @Transactional
    public ProfitCalculationResponse calculate(Long shipmentId, ProfitCalculationRequest request) {
        ProfitCalculation calculation = new ProfitCalculation();
        calculation.setShipment(getShipment(shipmentId));
        calculation.addIncome(newIncome(request.income()));
        calculation.addCost(newCost(COST_DESCRIPTION, request.cost()));
        if (hasValue(request.additionalCost())) {
            calculation.addCost(newCost(ADDITIONAL_COST_DESCRIPTION, request.additionalCost()));
        }

        BigDecimal totalIncome = sum(calculation.getIncomes(), Income::getAmount);
        BigDecimal totalCosts = sum(calculation.getCosts(), Cost::getAmount);
        calculation.setTotalIncome(totalIncome);
        calculation.setTotalCosts(totalCosts);
        calculation.setProfitOrLoss(totalIncome.subtract(totalCosts));

        // The income and costs are saved together with the calculation (cascade)
        ProfitCalculation saved = profitCalculationRepository.save(calculation);
        log.info("Profit calculated for shipment {}: income={}, costs={}, result={}",
                saved.getShipment().getShipmentCode(), totalIncome, totalCosts, saved.getProfitOrLoss());

        return profitCalculationMapper.toResponse(saved);
    }

    @Override
    public List<ProfitCalculationResponse> findHistory(Long shipmentId) {
        getShipment(shipmentId);
        return profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(shipmentId).stream()
                .map(profitCalculationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long shipmentId, Long calculationId) {
        ProfitCalculation calculation = profitCalculationRepository.findByIdAndShipmentId(calculationId, shipmentId)
                .orElseThrow(() -> new ProfitCalculationNotFoundException(calculationId));

        // The income and costs of the calculation are deleted with it (cascade)
        profitCalculationRepository.delete(calculation);
        log.info("Profit calculation {} of shipment {} deleted", calculationId, shipmentId);
    }

    private Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    private static Income newIncome(BigDecimal amount) {
        Income income = new Income();
        income.setDescription(INCOME_DESCRIPTION);
        income.setAmount(amount);
        return income;
    }

    private static Cost newCost(String description, BigDecimal amount) {
        Cost cost = new Cost();
        cost.setDescription(description);
        cost.setAmount(amount);
        return cost;
    }

    private static <T> BigDecimal sum(List<T> entries, Function<T, BigDecimal> amount) {
        return entries.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean hasValue(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
}
