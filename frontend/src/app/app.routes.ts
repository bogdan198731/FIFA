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
    path: 'admin',
    pathMatch: 'full',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-home-page.component').then(
        (m) => m.AdminHomePageComponent
      )
  },
  {
    path: 'admin/users',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-users-page.component').then(
        (m) => m.AdminUsersPageComponent
      )
  },
  {
    path: 'admin/config',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-config-page.component').then(
        (m) => m.AdminConfigPageComponent
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
  {
    path: 'admin/recalculate-scores',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-recalculate-scores-page.component').then(
        (m) => m.AdminRecalculateScoresPageComponent
      )
  },
  {
    path: 'admin/questions',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-questions-page.component').then(
        (m) => m.AdminQuestionsPageComponent
      )
  },
  { path: '**', redirectTo: '' }
];
