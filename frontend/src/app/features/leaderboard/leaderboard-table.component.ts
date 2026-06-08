import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { AuthService } from '../../core/services/auth.service';
import { I18nService } from '../../core/services/i18n.service';
import { LeaderboardService } from '../../core/services/leaderboard.service';
import { LeaderboardEntry } from '../../core/models/leaderboard.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-leaderboard-table',
  standalone: true,
  imports: [EmptyStateComponent, LoadingSpinnerComponent],
  templateUrl: './leaderboard-table.component.html',
  styleUrl: './leaderboard-table.component.scss'
})
export class LeaderboardTableComponent implements OnInit {
  private readonly leaderboardService = inject(LeaderboardService);
  private readonly auth = inject(AuthService);
  readonly i18n = inject(I18nService);

  readonly entries = signal<LeaderboardEntry[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  readonly currentUserId = computed(() => this.auth.currentUser()?.id ?? null);
  readonly isAuthenticated = this.auth.isAuthenticated;

  displayName(username: string): string {
    return this.isAuthenticated() ? username : username.slice(0, 1).toUpperCase() + '***';
  }

  ngOnInit(): void {
    this.leaderboardService.list().subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.state.set('ready');
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load leaderboard');
        this.state.set('error');
      }
    });
  }
}
