import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CalculationFormComponent } from '../../components/calculation-form/calculation-form.component';
import { CalculationHistoryComponent } from '../../components/calculation-history/calculation-history.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../components/confirm-dialog/confirm-dialog.component';
import { Calculation, CalculationRequest } from '../../models/calculation.model';
import { Shipment } from '../../models/shipment.model';
import { ProfitCalculationService } from '../../services/profit-calculation.service';

/**
 * "Calculate Profit" page.
 * <p>
 * Holds the page state (shipments, selected shipment, history) and talks to the backend.
 * The form and the history table are presentational child components.
 */
@Component({
  selector: 'app-calculate-profit',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
    CalculationFormComponent,
    CalculationHistoryComponent
  ],
  templateUrl: './calculate-profit.component.html'
})
export class CalculateProfitComponent implements OnInit {
  private readonly service = inject(ProfitCalculationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  @ViewChild(CalculationFormComponent) private form?: CalculationFormComponent;

  shipments: Shipment[] = [];
  selectedShipment: Shipment | null = null;
  history: Calculation[] = [];
  loading = false;

  ngOnInit(): void {
    this.loadShipments();
  }

  /** Loads the shipments and selects the first one. */
  loadShipments(): void {
    this.loading = true;
    this.service.getShipments().subscribe({
      next: shipments => {
        this.shipments = shipments;
        this.loading = false;
        if (shipments.length > 0) {
          this.selectShipment(shipments[0]);
        }
      },
      error: error => this.handleError(error)
    });
  }

  /** Changes the selected shipment and loads its calculation history. */
  selectShipment(shipment: Shipment): void {
    this.selectedShipment = shipment;
    this.history = [];
    this.loading = true;
    this.service.getHistory(shipment.id).subscribe({
      next: history => {
        this.history = history;
        this.loading = false;
      },
      error: error => this.handleError(error)
    });
  }

  /** Sends the form values to the backend and adds the result to the top of the history. */
  calculate(request: CalculationRequest): void {
    if (!this.selectedShipment) {
      return;
    }
    this.loading = true;
    this.service.calculate(this.selectedShipment.id, request).subscribe({
      next: result => {
        this.history = [result, ...this.history];
        this.loading = false;
        this.form?.reset();
        this.snackBar.open('Profit calculated', 'Close', { duration: 3000 });
      },
      error: error => this.handleError(error)
    });
  }

  /** Asks for confirmation, then deletes the calculation (with its income and costs). */
  delete(calculation: Calculation): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          title: 'Delete calculation',
          message: 'Delete this calculation? Its income and costs are deleted as well.',
          confirmLabel: 'Delete'
        }
      })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed && this.selectedShipment) {
          this.deleteConfirmed(this.selectedShipment, calculation);
        }
      });
  }

  private deleteConfirmed(shipment: Shipment, calculation: Calculation): void {
    this.loading = true;
    this.service.deleteCalculation(shipment.id, calculation.id).subscribe({
      next: () => {
        this.history = this.history.filter(item => item.id !== calculation.id);
        this.loading = false;
        this.snackBar.open('Calculation deleted', 'Close', { duration: 3000 });
      },
      error: error => this.handleError(error)
    });
  }

  /** Shows the backend error message to the user (use case alternative flow). */
  private handleError(error: HttpErrorResponse): void {
    this.loading = false;
    const message = error.error?.detail ?? 'Could not reach the server, please try again';
    this.snackBar.open(message, 'Close', { duration: 5000 });
  }
}
