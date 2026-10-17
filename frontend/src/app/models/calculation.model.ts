/**
 * Result of a profit or loss calculation of a shipment
 * (one row of the history table).
 */
export interface Calculation {
  id: number;
  shipmentId: number;
  shipmentCode: string;
  totalIncome: number;
  totalCosts: number;
  /** Total income minus total costs; negative means loss. */
  profitOrLoss: number;
  /** ISO date-time of the calculation. */
  calculatedAt: string;
}

/**
 * Values entered by the user in the calculation form.
 * Empty or zero values are not recorded by the backend.
 */
export interface CalculationRequest {
  income: number | null;
  cost: number | null;
  additionalCost: number | null;
}
