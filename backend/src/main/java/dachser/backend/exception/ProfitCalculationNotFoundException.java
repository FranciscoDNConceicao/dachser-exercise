package dachser.backend.exception;

/**
 * Thrown when the requested calculation does not exist (or belongs to another shipment).
 */
public class ProfitCalculationNotFoundException extends ResourceNotFoundException {

    public ProfitCalculationNotFoundException(Long calculationId) {
        super("Profit calculation not found: " + calculationId);
    }
}
