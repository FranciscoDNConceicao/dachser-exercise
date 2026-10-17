import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CalculationFormComponent } from '../../components/calculation-form/calculation-form.component';
import { CalculationHistoryComponent } from '../../components/calculation-history/calculation-history.component';
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

  /** Shows the backend error message to the user (use case alternative flow). */
  private handleError(error: HttpErrorResponse): void {
    this.loading = false;
    const message = error.error?.detail ?? 'Could not reach the server, please try again';
    this.snackBar.open(message, 'Close', { duration: 5000 });
  }
}
