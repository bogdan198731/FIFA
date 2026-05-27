import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { AuthService } from '../../core/services/auth.service';
import { LeaderboardService } from '../../core/services/leaderboard.service';
import { LeaderboardEntry } from '../../core/models/leaderboard.model';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-leaderboard-table',
  standalone: true,
  templateUrl: './leaderboard-table.component.html',
  styleUrl: './leaderboard-table.component.scss'
})
export class LeaderboardTableComponent implements OnInit {
  private readonly leaderboardService = inject(LeaderboardService);
  private readonly auth = inject(AuthService);

  readonly entries = signal<LeaderboardEntry[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  readonly currentUserId = computed(() => this.auth.currentUser()?.id ?? null);

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
