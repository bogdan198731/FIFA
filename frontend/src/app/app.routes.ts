import { Routes } from '@angular/router';

import { adminGuard, authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/home/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'leaderboard',
    loadComponent: () =>
      import('./features/leaderboard/leaderboard-table.component').then(
        (m) => m.LeaderboardTableComponent
      )
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile.component').then((m) => m.ProfileComponent)
  },
  {
    path: 'tournament-questions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/questions/tournament-questions-page.component').then(
        (m) => m.TournamentQuestionsPageComponent
      )
  },
  {
    path: 'predictions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/predictions/match-predictions-page.component').then(
        (m) => m.MatchPredictionsPageComponent
      )
  },
  {
    path: 'admin/matches',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-matches-page.component').then(
        (m) => m.AdminMatchesPageComponent
      )
  },
  {
    path: 'admin/results',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-results-page.component').then(
        (m) => m.AdminResultsPageComponent
      )
  },
  { path: '**', redirectTo: '' }
];
