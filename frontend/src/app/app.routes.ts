import { Routes } from '@angular/router';

import { CalculateProfitComponent } from './pages/calculate-profit/calculate-profit.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';

/**
 * Application pages.
 */
export const routes: Routes = [
  { path: '', redirectTo: 'calculate-profit', pathMatch: 'full' },
  { path: 'calculate-profit', component: CalculateProfitComponent, title: 'Calculate Profit' },
  { path: '**', component: NotFoundComponent, title: 'Page not found' }
];
