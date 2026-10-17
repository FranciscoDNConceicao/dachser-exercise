import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Calculation, CalculationRequest } from '../models/calculation.model';
import { Shipment } from '../models/shipment.model';

/**
 * HTTP access to the backend "Calculate Profit" API.
 * Requests to /api are forwarded to the backend by the dev server proxy (proxy.conf.json).
 */
@Injectable({ providedIn: 'root' })
export class ProfitCalculationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/shipments';

  /** Lists all shipments. */
  getShipments(): Observable<Shipment[]> {
    return this.http.get<Shipment[]>(this.baseUrl);
  }

  /** Returns the calculation history of a shipment, most recent first. */
  getHistory(shipmentId: number): Observable<Calculation[]> {
    return this.http.get<Calculation[]>(`${this.baseUrl}/${shipmentId}/profit-calculations`);
  }

  /** Records the entered income and costs and calculates the profit or loss of a shipment. */
  calculate(shipmentId: number, request: CalculationRequest): Observable<Calculation> {
    return this.http.post<Calculation>(`${this.baseUrl}/${shipmentId}/profit-calculations`, request);
  }
}
