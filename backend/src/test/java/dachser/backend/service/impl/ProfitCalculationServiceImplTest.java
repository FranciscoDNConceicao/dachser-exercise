package dachser.backend.service.impl;

import dachser.backend.dto.ProfitCalculationRequest;
import dachser.backend.dto.ProfitCalculationResponse;
import dachser.backend.entity.Cost;
import dachser.backend.entity.ProfitCalculation;
import dachser.backend.entity.Shipment;
import dachser.backend.exception.ProfitCalculationNotFoundException;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.mapper.ProfitCalculationMapper;
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
    private ProfitCalculationRepository profitCalculationRepository;

    private ProfitCalculationServiceImpl service;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        service = new ProfitCalculationServiceImpl(shipmentRepository, profitCalculationRepository,
                new ProfitCalculationMapper());

        shipment = new Shipment();
        shipment.setId(SHIPMENT_ID);
        shipment.setShipmentCode("SHP-0001");
    }

    @Test
    void calculate_returnsIncomeMinusCosts() {
        givenShipment();

        ProfitCalculationResponse result = service.calculate(SHIPMENT_ID, request("230.50", "200", "0"));

        assertThat(result.shipmentCode()).isEqualTo("SHP-0001");
        assertThat(result.totalIncome()).isEqualByComparingTo("230.50");
        assertThat(result.totalCosts()).isEqualByComparingTo("200");
        assertThat(result.profitOrLoss()).isEqualByComparingTo("30.50");
    }

    @Test
    void calculate_addsAdditionalCostToTotalCosts() {
        givenShipment();

        ProfitCalculationResponse result = service.calculate(SHIPMENT_ID, request("1000", "150", "50"));

        assertThat(result.totalCosts()).isEqualByComparingTo("200");
        assertThat(result.profitOrLoss()).isEqualByComparingTo("800");
    }

    @Test
    void calculate_returnsNegativeValueWhenCostsExceedIncome() {
        givenShipment();

        ProfitCalculationResponse result = service.calculate(SHIPMENT_ID, request("500", "900", null));

        assertThat(result.profitOrLoss()).isEqualByComparingTo("-400");
    }

    @Test
    void calculate_storesTheIncomeAndCostsWithTheCalculation() {
        givenShipment();

        service.calculate(SHIPMENT_ID, request("1000", "150", "50"));

        ProfitCalculation calculation = savedCalculation();
        assertThat(calculation.getShipment()).isSameAs(shipment);
        assertThat(calculation.getTotalIncome()).isEqualByComparingTo("1000");
        assertThat(calculation.getTotalCosts()).isEqualByComparingTo("200");
        assertThat(calculation.getProfitOrLoss()).isEqualByComparingTo("800");
        assertThat(calculation.getIncomes()).singleElement().satisfies(income -> {
            assertThat(income.getAmount()).isEqualByComparingTo("1000");
            assertThat(income.getDescription()).isEqualTo(ProfitCalculationServiceImpl.INCOME_DESCRIPTION);
            assertThat(income.getCalculation()).isSameAs(calculation);
        });
        assertThat(calculation.getCosts())
                .extracting(Cost::getDescription)
                .containsExactly(ProfitCalculationServiceImpl.COST_DESCRIPTION,
                        ProfitCalculationServiceImpl.ADDITIONAL_COST_DESCRIPTION);
        assertThat(calculation.getCosts())
                .allSatisfy(cost -> assertThat(cost.getCalculation()).isSameAs(calculation));
    }

    @Test
    void calculate_doesNotRecordEmptyOrZeroAdditionalCost() {
        givenShipment();

        service.calculate(SHIPMENT_ID, request("100", "50", "0"));

        assertThat(savedCalculation().getCosts())
                .extracting(Cost::getDescription)
                .containsExactly(ProfitCalculationServiceImpl.COST_DESCRIPTION);
    }

    @Test
    void calculate_throwsWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate(99L, request("10", "5", null)))
                .isInstanceOf(ShipmentNotFoundException.class);
        verifyNoInteractions(profitCalculationRepository);
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

    @Test
    void delete_removesTheCalculation() {
        ProfitCalculation calculation = new ProfitCalculation();
        when(profitCalculationRepository.findByIdAndShipmentId(7L, SHIPMENT_ID)).thenReturn(Optional.of(calculation));

        service.delete(SHIPMENT_ID, 7L);

        verify(profitCalculationRepository).delete(calculation);
    }

    @Test
    void delete_throwsWhenCalculationDoesNotBelongToTheShipment() {
        when(profitCalculationRepository.findByIdAndShipmentId(7L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(2L, 7L))
                .isInstanceOf(ProfitCalculationNotFoundException.class)
                .hasMessageContaining("7");
        verify(profitCalculationRepository, never()).delete(any());
    }

    /**
     * Mocks an existing shipment and the save of the calculation.
     */
    private void givenShipment() {
        when(shipmentRepository.findById(SHIPMENT_ID)).thenReturn(Optional.of(shipment));
        when(profitCalculationRepository.save(any(ProfitCalculation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ProfitCalculation savedCalculation() {
        ArgumentCaptor<ProfitCalculation> saved = ArgumentCaptor.forClass(ProfitCalculation.class);
        verify(profitCalculationRepository).save(saved.capture());
        return saved.getValue();
    }

    private static ProfitCalculationRequest request(String income, String cost, String additionalCost) {
        return new ProfitCalculationRequest(decimal(income), decimal(cost), decimal(additionalCost));
    }

    private static BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
