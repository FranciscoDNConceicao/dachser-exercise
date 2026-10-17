package dachser.backend.service.impl;

import dachser.backend.dto.ShipmentResponse;
import dachser.backend.entity.Customer;
import dachser.backend.entity.Shipment;
import dachser.backend.exception.ShipmentNotFoundException;
import dachser.backend.mapper.ShipmentMapper;
import dachser.backend.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the shipment read operations (repository is mocked).
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShipmentServiceImpl service;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        service = new ShipmentServiceImpl(shipmentRepository, new ShipmentMapper());

        Customer customer = new Customer();
        customer.setName("Atlantic Trading Lda");

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setShipmentCode("SHP-0001");
        shipment.setDescription("Shipment Porto - Lisbon");
        shipment.setCustomer(customer);
    }

    @Test
    void findAll_returnsMappedShipments() {
        when(shipmentRepository.findAll()).thenReturn(List.of(shipment));

        assertThat(service.findAll())
                .containsExactly(new ShipmentResponse(1L, "SHP-0001", "Shipment Porto - Lisbon", "Atlantic Trading Lda"));
    }

    @Test
    void findByCode_returnsShipment() {
        when(shipmentRepository.findByShipmentCode("SHP-0001")).thenReturn(Optional.of(shipment));

        assertThat(service.findByCode("SHP-0001").customerName()).isEqualTo("Atlantic Trading Lda");
    }

    @Test
    void findByCode_throwsWhenShipmentDoesNotExist() {
        when(shipmentRepository.findByShipmentCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("NOPE"))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining("NOPE");
    }
}
