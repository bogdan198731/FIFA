import { Component, OnInit, inject, signal } from '@angular/core';

import { AuthService } from '../../core/services/auth.service';
import { HealthService } from '../../core/services/health.service';
import { HealthStatus } from '../../core/models/health-status.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly healthService = inject(HealthService);

  readonly currentUser = this.auth.currentUser;
  readonly health = signal<HealthStatus | null>(null);
  readonly healthError = signal<string | null>(null);

  ngOnInit(): void {
    this.healthService.check().subscribe({
      next: (status) => this.health.set(status),
      error: (err) => this.healthError.set(err?.message ?? 'Backend unreachable')
    });
  }
}
