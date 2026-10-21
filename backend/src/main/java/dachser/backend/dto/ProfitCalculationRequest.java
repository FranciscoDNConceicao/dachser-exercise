package dachser.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Data entered by the user to calculate the profit or loss of a shipment
 * (fields of the "Income &amp; Outcome" form).
 * <p>
 * Each request creates a new, independent calculation:
 * {@code profit or loss = income - (cost + additional cost)}.
 *
 * @param income         customer payment (required)
 * @param cost           service cost (required)
 * @param additionalCost additional cost (optional; empty or zero is not recorded)
 */
public record ProfitCalculationRequest(
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal income,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal cost,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal additionalCost
) {
}
