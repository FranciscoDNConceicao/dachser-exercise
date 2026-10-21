package dachser.backend.exception;

/**
 * Thrown when the requested shipment does not exist.
 */
public class ShipmentNotFoundException extends ResourceNotFoundException {

    public ShipmentNotFoundException(Long shipmentId) {
        super("Shipment not found: " + shipmentId);
    }

    public ShipmentNotFoundException(String shipmentCode) {
        super("Shipment not found: " + shipmentCode);
    }
}
