import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Calculation } from '../models/calculation.model';
import { ProfitCalculationService } from './profit-calculation.service';

describe('ProfitCalculationService', () => {
  let service: ProfitCalculationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProfitCalculationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should get the shipments', () => {
    service.getShipments().subscribe(shipments => expect(shipments.length).toBe(1));

    const request = http.expectOne('/api/shipments');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, shipmentCode: 'SHP-0001', description: 'Porto - Lisbon', customerName: 'Atlantic' }]);
  });

  it('should get the history of a shipment', () => {
    service.getHistory(1).subscribe(history => expect(history).toEqual([]));

    const request = http.expectOne('/api/shipments/1/profit-calculations');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should post the calculation request', () => {
    const result = { id: 5, profitOrLoss: 730.5 } as Calculation;
    const body = { income: 230.5, cost: 200, additionalCost: 0 };

    service.calculate(1, body).subscribe(calculation => expect(calculation).toEqual(result));

    const request = http.expectOne('/api/shipments/1/profit-calculations');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(result);
  });
});
