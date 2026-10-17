package dachser.backend.dto;

/**
 * Shipment data exposed to the client (used to search and select a shipment).
 *
 * @param id           technical identifier
 * @param shipmentCode business code (e.g. {@code SHP-0001})
 * @param description  free text description
 * @param customerName name of the customer that pays for the shipment
 */
public record ShipmentResponse(
        Long id,
        String shipmentCode,
        String description,
        String customerName
) {
}
