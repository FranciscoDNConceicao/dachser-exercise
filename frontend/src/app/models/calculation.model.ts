/**
 * Result of a profit or loss calculation of a shipment
 * (one row of the history table). Each calculation is independent.
 */
export interface Calculation {
  id: number;
  shipmentId: number;
  shipmentCode: string;
  /** Income entered in the calculation. */
  totalIncome: number;
  /** Cost plus additional cost entered in the calculation. */
  totalCosts: number;
  /** Total income minus total costs; negative means loss. */
  profitOrLoss: number;
  /** ISO date-time of the calculation. */
  calculatedAt: string;
}

/**
 * Values entered by the user in the calculation form.
 * Income and cost are required; an empty or zero additional cost is not recorded.
 */
export interface CalculationRequest {
  income: number;
  cost: number;
  additionalCost: number | null;
}
