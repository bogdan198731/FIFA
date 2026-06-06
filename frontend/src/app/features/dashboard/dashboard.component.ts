import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { I18nService } from '../../core/services/i18n.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardSummary } from '../../core/models/dashboard.model';
import { teamFlag } from '../../shared/team-flags';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);
  readonly i18n = inject(I18nService);

  readonly currentUser = this.auth.currentUser;
  readonly dashboard = signal<DashboardSummary | null>(null);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);
  readonly teamFlag = teamFlag;

  ngOnInit(): void {
    this.dashboardService.get().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
        this.state.set('ready');
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load dashboard.');
        this.state.set('error');
      }
    });
  }
}
