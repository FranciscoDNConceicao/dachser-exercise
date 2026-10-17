import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';

import { Calculation } from '../../models/calculation.model';

/**
 * Table with the calculation history of a shipment
 * (income, total costs and profit or loss).
 */
@Component({
  selector: 'app-calculation-history',
  standalone: true,
  imports: [MatTableModule, DecimalPipe, DatePipe],
  templateUrl: './calculation-history.component.html'
})
export class CalculationHistoryComponent {
  /** Calculations to show, most recent first. */
  @Input() calculations: Calculation[] = [];

  readonly columns = ['calculatedAt', 'totalIncome', 'totalCosts', 'profitOrLoss'];
}
