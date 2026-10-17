package dachser.backend.repository;

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
 * Repository tests against an in-memory H2 database loaded with {@code schema.sql}
 * (validates the custom queries and the database constraints).
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
    void sumAmountByShipmentId_sumsSampleData() {
        Long id = shipmentId("SHP-0001");

        assertThat(incomeRepository.sumAmountByShipmentId(id)).isEqualByComparingTo("1500.00");
        assertThat(costRepository.sumAmountByShipmentId(id)).isEqualByComparingTo("800.00");
    }

    @Test
    void sumAmountByShipmentId_returnsZeroWithoutRecords() {
        assertThat(incomeRepository.sumAmountByShipmentId(999L)).isEqualByComparingTo("0");
        assertThat(costRepository.sumAmountByShipmentId(999L)).isEqualByComparingTo("0");
    }

    @Test
    void findByShipmentIdOrderByCalculatedAtDesc_returnsMostRecentFirst() {
        assertThat(profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(shipmentId("SHP-0001")))
                .extracting(ProfitCalculation::getProfitOrLoss)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("700.00"), new BigDecimal("900.00"));
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
}
