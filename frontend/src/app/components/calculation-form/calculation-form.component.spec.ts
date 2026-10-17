import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { CalculationRequest } from '../../models/calculation.model';
import { CalculationFormComponent } from './calculation-form.component';

describe('CalculationFormComponent', () => {
  let fixture: ComponentFixture<CalculationFormComponent>;
  let component: CalculationFormComponent;
  let emitted: CalculationRequest[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalculationFormComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(CalculationFormComponent);
    component = fixture.componentInstance;
    emitted = [];
    component.calculate.subscribe(request => emitted.push(request));
    fixture.detectChanges();
  });

  it('should emit the values when the form is valid', () => {
    component.form.setValue({ income: 230.5, cost: 200, additionalCost: 0 });

    component.submit();

    expect(emitted).toEqual([{ income: 230.5, cost: 200, additionalCost: 0 }]);
  });

  it('should not emit when a value is negative', () => {
    component.form.setValue({ income: -1, cost: null, additionalCost: 0 });

    component.submit();

    expect(emitted).toEqual([]);
    expect(component.form.controls.income.touched).toBeTrue();
  });

  it('should clear the values on reset', () => {
    component.form.setValue({ income: 10, cost: 20, additionalCost: 30 });

    component.reset();

    expect(component.form.getRawValue()).toEqual({ income: null, cost: null, additionalCost: 0 });
  });
});
