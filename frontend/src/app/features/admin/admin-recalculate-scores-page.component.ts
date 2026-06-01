import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import {
  Match,
  ScoreRecalculationResponse
} from '../../core/models/match-prediction.model';
import { AdminScoreService } from '../../core/services/admin-score.service';
import { MatchService } from '../../core/services/match.service';
import { ExternalSyncButtonComponent } from './external-sync-button.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-recalculate-scores-page',
  standalone: true,
  imports: [ExternalSyncButtonComponent],
  templateUrl: './admin-recalculate-scores-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminRecalculateScoresPageComponent implements OnInit {
  private readonly matchService = inject(MatchService);
  private readonly adminScoreService = inject(AdminScoreService);

  readonly matches = signal<Match[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);
  readonly result = signal<ScoreRecalculationResponse | null>(null);
  readonly recalculatingAll = signal(false);
  readonly recalculatingMatchId = signal<number | null>(null);

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

  recalculateAll(): void {
    this.recalculatingAll.set(true);
    this.errorMessage.set(null);
    this.adminScoreService.recalculateAll().subscribe({
      next: (result) => {
        this.result.set(result);
        this.recalculatingAll.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Could not recalculate scores.');
        this.recalculatingAll.set(false);
      }
    });
  }

  recalculateMatch(match: Match): void {
    this.recalculatingMatchId.set(match.id);
    this.errorMessage.set(null);
    this.adminScoreService.recalculateMatch(match.id).subscribe({
      next: (result) => {
        this.result.set(result);
        this.recalculatingMatchId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Could not recalculate this match.');
        this.recalculatingMatchId.set(null);
      }
    });
  }
}
