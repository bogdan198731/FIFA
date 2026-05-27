import { Component, OnInit, inject, signal } from '@angular/core';

import { MatchFormComponent } from './match-form.component';
import { Match } from '../../core/models/match-prediction.model';
import { MatchService } from '../../core/services/match.service';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-matches-page',
  standalone: true,
  imports: [MatchFormComponent],
  templateUrl: './admin-matches-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminMatchesPageComponent implements OnInit {
  private readonly matchService = inject(MatchService);

  readonly matches = signal<Match[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.matchService.list().subscribe({
      next: (matches) => {
        this.matches.set(matches);
        this.state.set('ready');
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load matches.');
        this.state.set('error');
      }
    });
  }

  upsert(match: Match): void {
    this.matches.update((matches) => {
      const exists = matches.some((m) => m.id === match.id);
      const next = exists ? matches.map((m) => (m.id === match.id ? match : m)) : [...matches, match];
      return next.sort((a, b) => a.kickoffAt.localeCompare(b.kickoffAt));
    });
  }

  remove(matchId: number): void {
    this.matches.update((matches) => matches.filter((match) => match.id !== matchId));
  }
}
