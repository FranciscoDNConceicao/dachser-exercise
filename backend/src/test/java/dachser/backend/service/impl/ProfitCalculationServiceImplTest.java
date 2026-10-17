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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the "Calculate Profit" business logic (repositories are mocked).
 */
@ExtendWith(MockitoExtension.class)
class ProfitCalculationServiceImplTest {

    private static final Long SHIPMENT_ID = 1L;

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private CostRepository costRepository;
    @Mock
    private ProfitCalculationRepository profitCalculationRepository;

    private ProfitCalculationServiceImpl service;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        service = new ProfitCalculationServiceImpl(shipmentRepository, incomeRepository, costRepository,
                profitCalculationRepository, new ProfitCalculationMapper());

        shipment = new Shipment();
        shipment.setId(SHIPMENT_ID);
        shipment.setShipmentCode("SHP-0001");
    }

    @Test
    void calculate_recordsIncomeAndCostsAndReturnsProfit() {
        givenShipmentWithTotals("1230.50", "1000.00");

        ProfitCalculationResponse result = service.calculate(SHIPMENT_ID,
                new ProfitCalculationRequest(new BigDecimal("230.50"), new BigDecimal("200"), new BigDecimal("50")));

        ArgumentCaptor<Income> income = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(income.capture());
        assertThat(income.getValue().getAmount()).isEqualByComparingTo("230.50");
        assertThat(income.getValue().getShipment()).isSameAs(shipment);

        ArgumentCaptor<Cost> costs = ArgumentCaptor.forClass(Cost.class);
        verify(costRepository, times(2)).save(costs.capture());
        assertThat(costs.getAllValues())
                .extracting(Cost::getDescription)
                .containsExactly(ProfitCalculationServiceImpl.COST_DESCRIPTION,
                        ProfitCalculationServiceImpl.ADDITIONAL_COST_DESCRIPTION);

        assertThat(result.shipmentCode()).isEqualTo("SHP-0001");
        assertThat(result.totalIncome()).isEqualByComparingTo("1230.50");
        assertThat(result.totalCosts()).isEqualByComparingTo("1000.00");
        assertThat(result.profitOrLoss()).isEqualByComparingTo("230.50");
    }

    @Test
    void calculate_returnsNegativeValueWhenCostsExceedIncome() {
        givenShipmentWithTotals("500.00", "900.00");

        ProfitCalculationResponse result = service.calculate(SHIPMENT_ID,
                new ProfitCalculationRequest(null, null, null));

        assertThat(result.profitOrLoss()).isEqualByComparingTo("-400.00");
    }

    @Test
    void calculate_storesTheCalculationResult() {
        givenShipmentWithTotals("1000.00", "200.00");

        service.calculate(SHIPMENT_ID, new ProfitCalculationRequest(null, null, null));

        ArgumentCaptor<ProfitCalculation> saved = ArgumentCaptor.forClass(ProfitCalculation.class);
        verify(profitCalculationRepository).save(saved.capture());
        assertThat(saved.getValue().getShipment()).isSameAs(shipment);
        assertThat(saved.getValue().getTotalIncome()).isEqualByComparingTo("1000.00");
        assertThat(saved.getValue().getTotalCosts()).isEqualByComparingTo("200.00");
        assertThat(saved.getValue().getProfitOrLoss()).isEqualByComparingTo("800.00");
    }

    @Test
    void calculate_ignoresEmptyAndZeroAmounts() {
        givenShipmentWithTotals("1500.00", "800.00");

        service.calculate(SHIPMENT_ID, new ProfitCalculationRequest(BigDecimal.ZERO, null, BigDecimal.ZERO));

        verify(incomeRepository, never()).save(any());
        verify(costRepository, never()).save(any());
    }

    @Test
    void calculate_throwsWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate(99L, new ProfitCalculationRequest(BigDecimal.TEN, null, null)))
                .isInstanceOf(ShipmentNotFoundException.class);
        verifyNoInteractions(incomeRepository, costRepository, profitCalculationRepository);
    }

    @Test
    void findHistory_returnsMappedCalculations() {
        when(shipmentRepository.findById(SHIPMENT_ID)).thenReturn(Optional.of(shipment));
        ProfitCalculation calculation = new ProfitCalculation();
        calculation.setId(7L);
        calculation.setShipment(shipment);
        calculation.setTotalIncome(new BigDecimal("1000"));
        calculation.setTotalCosts(new BigDecimal("200"));
        calculation.setProfitOrLoss(new BigDecimal("800"));
        when(profitCalculationRepository.findByShipmentIdOrderByCalculatedAtDesc(SHIPMENT_ID))
                .thenReturn(List.of(calculation));

        List<ProfitCalculationResponse> history = service.findHistory(SHIPMENT_ID);

        assertThat(history).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(7L);
            assertThat(item.profitOrLoss()).isEqualByComparingTo("800");
        });
    }

    @Test
    void findHistory_throwsWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findHistory(99L)).isInstanceOf(ShipmentNotFoundException.class);
    }

    /**
     * Mocks an existing shipment whose recorded incomes and costs sum to the given totals.
     */
    private void givenShipmentWithTotals(String totalIncome, String totalCosts) {
        when(shipmentRepository.findById(SHIPMENT_ID)).thenReturn(Optional.of(shipment));
        when(incomeRepository.sumAmountByShipmentId(SHIPMENT_ID)).thenReturn(new BigDecimal(totalIncome));
        when(costRepository.sumAmountByShipmentId(SHIPMENT_ID)).thenReturn(new BigDecimal(totalCosts));
        when(profitCalculationRepository.save(any(ProfitCalculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
