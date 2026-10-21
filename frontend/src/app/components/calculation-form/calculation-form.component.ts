import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { CalculationRequest } from '../../models/calculation.model';

/**
 * "Income & Outcome" form: income, cost and additional cost of a shipment.
 * <p>
 * Presentational component: it only validates the values and emits them through
 * {@link calculate}; the parent page sends them to the backend.
 */
@Component({
  selector: 'app-calculation-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './calculation-form.component.html'
})
export class CalculationFormComponent {
  /** Disables the Calculate button (e.g. while a request is running). */
  @Input() disabled = false;

  /** Emitted with the form values when the user clicks Calculate. */
  @Output() calculate = new EventEmitter<CalculationRequest>();

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    income: [null as number | null, [Validators.required, Validators.min(0)]],
    cost: [null as number | null, [Validators.required, Validators.min(0)]],
    additionalCost: [0 as number | null, Validators.min(0)]
  });

  /** Emits the values if the form is valid; otherwise shows the validation errors. */
  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { income, cost, additionalCost } = this.form.getRawValue();
    // Income and cost are guaranteed by the required validators
    this.calculate.emit({ income: income!, cost: cost!, additionalCost });
  }

  /** Clears the form after a successful calculation (called by the parent page). */
  reset(): void {
    this.form.reset({ income: null, cost: null, additionalCost: 0 });
  }
}
