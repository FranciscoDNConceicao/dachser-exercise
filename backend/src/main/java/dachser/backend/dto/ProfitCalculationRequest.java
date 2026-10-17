package dachser.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Data entered by the user to calculate the profit or loss of a shipment
 * (fields of the "Income &amp; Outcome" form).
 * <p>
 * All fields are optional: values that are empty or zero are not recorded,
 * and the calculation uses everything already recorded for the shipment.
 *
 * @param income         new customer payment
 * @param cost           new service cost
 * @param additionalCost new additional cost
 */
public record ProfitCalculationRequest(
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal income,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal cost,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal additionalCost
) {
}
