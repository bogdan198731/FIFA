import { Component, OnInit, inject, signal } from '@angular/core';

import { ResultFormComponent } from './result-form.component';
import { ExternalSyncButtonComponent } from './external-sync-button.component';
import { Match } from '../../core/models/match-prediction.model';
import { MatchService } from '../../core/services/match.service';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-results-page',
  standalone: true,
  imports: [ResultFormComponent, ExternalSyncButtonComponent],
  templateUrl: './admin-results-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminResultsPageComponent implements OnInit {
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

  replace(match: Match): void {
    this.matches.update((matches) => matches.map((m) => (m.id === match.id ? match : m)));
  }
}
