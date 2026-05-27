import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HealthService } from './core/services/health.service';
import { HealthStatus } from './core/models/health-status.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private readonly healthService = inject(HealthService);

  readonly title = 'World Cup Prediction Game';
  readonly health = signal<HealthStatus | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.healthService.check().subscribe({
      next: (status) => this.health.set(status),
      error: (err) => this.error.set(err?.message ?? 'Backend unreachable')
    });
  }
}
