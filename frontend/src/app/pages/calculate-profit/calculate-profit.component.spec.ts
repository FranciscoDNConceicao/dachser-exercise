import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { Calculation } from '../../models/calculation.model';
import { Shipment } from '../../models/shipment.model';
import { ProfitCalculationService } from '../../services/profit-calculation.service';
import { CalculateProfitComponent } from './calculate-profit.component';

describe('CalculateProfitComponent', () => {
  const shipment: Shipment = { id: 1, shipmentCode: 'SHP-0001', description: 'Porto - Lisbon', customerName: 'Atlantic' };
  const oldCalculation = { id: 2, profitOrLoss: 700 } as Calculation;
  const newCalculation = { id: 5, profitOrLoss: 730.5 } as Calculation;

  let fixture: ComponentFixture<CalculateProfitComponent>;
  let component: CalculateProfitComponent;
  let service: jasmine.SpyObj<ProfitCalculationService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    service = jasmine.createSpyObj('ProfitCalculationService', ['getShipments', 'getHistory', 'calculate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    service.getShipments.and.returnValue(of([shipment]));
    service.getHistory.and.returnValue(of([oldCalculation]));

    await TestBed.configureTestingModule({
      imports: [CalculateProfitComponent],
      providers: [
        provideNoopAnimations(),
        { provide: ProfitCalculationService, useValue: service },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CalculateProfitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should select the first shipment and load its history', () => {
    expect(component.selectedShipment).toBe(shipment);
    expect(service.getHistory).toHaveBeenCalledWith(1);
    expect(component.history).toEqual([oldCalculation]);
  });

  it('should add the new calculation to the top of the history', () => {
    service.calculate.and.returnValue(of(newCalculation));

    component.calculate({ income: 230.5, cost: 200, additionalCost: 0 });

    expect(service.calculate).toHaveBeenCalledWith(1, { income: 230.5, cost: 200, additionalCost: 0 });
    expect(component.history).toEqual([newCalculation, oldCalculation]);
  });

  it('should show the backend error message', () => {
    service.calculate.and.returnValue(throwError(() =>
      new HttpErrorResponse({ status: 404, error: { detail: 'Shipment not found: 1' } })));

    component.calculate({ income: 10, cost: null, additionalCost: 0 });

    expect(snackBar.open).toHaveBeenCalledWith('Shipment not found: 1', 'Close', jasmine.any(Object));
    expect(component.loading).toBeFalse();
  });
});
