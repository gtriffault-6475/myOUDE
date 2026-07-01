import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      // Les routes métier seront ajoutées dans les stories E2+
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  }
];
