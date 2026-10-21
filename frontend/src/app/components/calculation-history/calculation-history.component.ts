import { DatePipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';

import { Calculation } from '../../models/calculation.model';

/**
 * Table with the calculation history of a shipment
 * (income, total costs and profit or loss). Columns can be sorted by clicking the header.
 * <p>
 * Presentational component: the delete button only emits the row through {@link remove}.
 */
@Component({
  selector: 'app-calculation-history',
  standalone: true,
  imports: [MatTableModule, MatSortModule, MatButtonModule, MatIconModule, DecimalPipe, DatePipe],
  templateUrl: './calculation-history.component.html'
})
export class CalculationHistoryComponent implements AfterViewInit {
  /** Table data; MatTableDataSource takes care of the sorting. */
  readonly dataSource = new MatTableDataSource<Calculation>([]);

  readonly columns = ['calculatedAt', 'totalIncome', 'totalCosts', 'profitOrLoss', 'actions'];

  /** Disables the delete buttons (e.g. while a request is running). */
  @Input() disabled = false;

  /** Emitted with the calculation whose delete button was clicked. */
  @Output() remove = new EventEmitter<Calculation>();

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
