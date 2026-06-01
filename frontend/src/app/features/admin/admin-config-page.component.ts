import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { AdminConfigService } from '../../core/services/admin-config.service';
import { AdminConfig } from '../../core/models/admin-user.model';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-config-page',
  standalone: true,
  templateUrl: './admin-config-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminConfigPageComponent implements OnInit {
  private readonly adminConfigService = inject(AdminConfigService);

  readonly config = signal<AdminConfig | null>(null);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.adminConfigService.get().subscribe({
      next: (config) => {
        this.config.set(config);
        this.state.set('ready');
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Could not load configuration.');
        this.state.set('error');
      }
    });
  }
}
