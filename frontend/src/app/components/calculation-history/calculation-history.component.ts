import { DatePipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, Component, Input, ViewChild } from '@angular/core';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';

import { Calculation } from '../../models/calculation.model';

/**
 * Table with the calculation history of a shipment
 * (income, total costs and profit or loss). Columns can be sorted by clicking the header.
 */
@Component({
  selector: 'app-calculation-history',
  standalone: true,
  imports: [MatTableModule, MatSortModule, DecimalPipe, DatePipe],
  templateUrl: './calculation-history.component.html'
})
export class CalculationHistoryComponent implements AfterViewInit {
  /** Table data; MatTableDataSource takes care of the sorting. */
  readonly dataSource = new MatTableDataSource<Calculation>([]);

  readonly columns = ['calculatedAt', 'totalIncome', 'totalCosts', 'profitOrLoss'];

  @ViewChild(MatSort) private sort!: MatSort;

  /** Calculations to show, most recent first (until the user sorts by a column). */
  @Input() set calculations(value: Calculation[]) {
    this.dataSource.data = value;
  }

  /** Connects the sort headers to the data once the table exists. */
  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }
}
