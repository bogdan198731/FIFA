import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { I18nService } from '../../core/services/i18n.service';
import { LeagueService } from '../../core/services/league.service';
import { AuthService } from '../../core/services/auth.service';
import { LeaderboardEntry } from '../../core/models/leaderboard.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-league-detail-page',
  standalone: true,
  imports: [RouterLink, LoadingSpinnerComponent, EmptyStateComponent],
  templateUrl: './league-detail-page.component.html',
  styleUrl: './league-detail-page.component.scss',
})
export class LeagueDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly leagueService = inject(LeagueService);
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
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.leagueService.leaderboard(id).subscribe({
      next: (entries) => { this.entries.set(entries); this.state.set('ready'); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load leaderboard');
        this.state.set('error');
      }
    });
  }
}
