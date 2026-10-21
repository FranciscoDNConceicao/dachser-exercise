package dachser.backend.repository;

import dachser.backend.entity.Cost;
import dachser.backend.entity.Income;
import dachser.backend.entity.ProfitCalculation;
import dachser.backend.entity.Shipment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository tests against an in-memory H2 database loaded with {@code schema.sql} and {@code data.sql}
 * (validates the custom queries, the cascades and the database constraints).
 */
@DataJpaTest
class RepositoryIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private IncomeRepository incomeRepository;
    @Autowired
    private CostRepository costRepository;
    @Autowired
    private ProfitCalculationRepository profitCalculationRepository;

    @Test
    void findByShipmentCode_loadsShipmentWithCustomer() {
        Shipment shipment = shipmentRepository.findByShipmentCode("SHP-0001").orElseThrow();

        assertThat(shipment.getCustomer().getName()).isEqualTo("Atlantic Trading Lda");
    }

    @Test
    void findByShipmentIdOrderByCalculatedAtDesc_returnsMostRecentFirst() {
        // The sample data reproduces the UI prototype: 1000 / 200 / 800 and 500 / 900 / -400
        assertThat(profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(shipmentId("SHP-0001")))
                .extracting(ProfitCalculation::getProfitOrLoss)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("800.00"), new BigDecimal("-400.00"));
    }

    @Test
    void calculation_ownsItsIncomeAndCosts() {
        ProfitCalculation calculation = latestCalculation("SHP-0002");

        assertThat(calculation.getIncomes())
                .extracting(Income::getAmount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("800.00"));
        assertThat(calculation.getCosts())
                .extracting(Cost::getDescription)
                .containsExactlyInAnyOrder("Cost", "Additional cost");
    }

    @Test
    void save_storesTheIncomeAndCostsWithTheCalculation() {
        long incomes = incomeRepository.count();
        long costs = costRepository.count();

        profitCalculationRepository.saveAndFlush(newCalculation("100.00", "60.00"));

        assertThat(incomeRepository.count()).isEqualTo(incomes + 1);
        assertThat(costRepository.count()).isEqualTo(costs + 1);
    }

    @Test
    void delete_removesTheIncomeAndCostsOfTheCalculation() {
        ProfitCalculation calculation = latestCalculation("SHP-0002"); // 1 income, 2 costs
        long incomes = incomeRepository.count();
        long costs = costRepository.count();

        profitCalculationRepository.delete(calculation);
        profitCalculationRepository.flush();

        assertThat(incomeRepository.count()).isEqualTo(incomes - 1);
        assertThat(costRepository.count()).isEqualTo(costs - 2);
    }

    @Test
    void findByIdAndShipmentId_ignoresCalculationsOfOtherShipments() {
        Long calculationId = latestCalculation("SHP-0002").getId();

        assertThat(profitCalculationRepository.findByIdAndShipmentId(calculationId, shipmentId("SHP-0002"))).isPresent();
        assertThat(profitCalculationRepository.findByIdAndShipmentId(calculationId, shipmentId("SHP-0001"))).isEmpty();
    }

    @Test
    void save_rejectsInconsistentProfit() {
        ProfitCalculation calculation = new ProfitCalculation();
        calculation.setShipment(shipmentRepository.findByShipmentCode("SHP-0001").orElseThrow());
        calculation.setTotalIncome(new BigDecimal("100.00"));
        calculation.setTotalCosts(new BigDecimal("50.00"));
        calculation.setProfitOrLoss(new BigDecimal("999.00"));

        assertThatThrownBy(() -> profitCalculationRepository.saveAndFlush(calculation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long shipmentId(String code) {
        return shipmentRepository.findByShipmentCode(code).orElseThrow().getId();
    }

    private ProfitCalculation latestCalculation(String shipmentCode) {
        return profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(shipmentId(shipmentCode)).get(0);
    }

    /**
     * Builds a consistent calculation of SHP-0001 with one income and one cost.
     */
    private ProfitCalculation newCalculation(String income, String cost) {
        ProfitCalculation calculation = new ProfitCalculation();
        calculation.setShipment(shipmentRepository.findByShipmentCode("SHP-0001").orElseThrow());

        Income newIncome = new Income();
        newIncome.setDescription("Customer payment");
        newIncome.setAmount(new BigDecimal(income));
        calculation.addIncome(newIncome);

        Cost newCost = new Cost();
        newCost.setDescription("Cost");
        newCost.setAmount(new BigDecimal(cost));
        calculation.addCost(newCost);

        calculation.setTotalIncome(newIncome.getAmount());
        calculation.setTotalCosts(newCost.getAmount());
        calculation.setProfitOrLoss(newIncome.getAmount().subtract(newCost.getAmount()));
        return calculation;
    }
}
