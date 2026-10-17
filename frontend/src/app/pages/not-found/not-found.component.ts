import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

/**
 * Page shown for unknown routes.
 */
@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, MatButtonModule],
  template: `
    <h2 class="h4">Page not found</h2>
    <p class="text-muted">The page you are looking for does not exist.</p>
    <a mat-flat-button color="primary" routerLink="/calculate-profit">Go to Calculate Profit</a>
  `
})
export class NotFoundComponent {}
