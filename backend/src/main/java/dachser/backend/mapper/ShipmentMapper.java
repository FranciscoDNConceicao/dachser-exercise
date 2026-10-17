package dachser.backend.mapper;

import dachser.backend.dto.ShipmentResponse;
import dachser.backend.entity.Shipment;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Shipment} entities to {@link ShipmentResponse} DTOs.
 */
@Component
public class ShipmentMapper {

    /**
     * Converts a shipment entity into its DTO.
     * The customer must be loaded (or the call made inside a transaction).
     */
    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getDescription(),
                shipment.getCustomer().getName()
        );
    }
}
