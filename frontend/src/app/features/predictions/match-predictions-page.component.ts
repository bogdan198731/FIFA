import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { MatchCardComponent } from './match-card.component';
import {
  Match,
  MatchPrediction
} from '../../core/models/match-prediction.model';
import { MatchService } from '../../core/services/match.service';
import { PredictionService } from '../../core/services/prediction.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-match-predictions-page',
  standalone: true,
  imports: [EmptyStateComponent, LoadingSpinnerComponent, MatchCardComponent],
  templateUrl: './match-predictions-page.component.html',
  styleUrl: './match-predictions-page.component.scss'
})
export class MatchPredictionsPageComponent implements OnInit {
  private readonly matchService = inject(MatchService);
  private readonly predictionService = inject(PredictionService);

  readonly matches = signal<Match[]>([]);
  readonly predictionsByMatchId = signal<Map<number, MatchPrediction>>(new Map());
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  readonly openCount = computed(() => this.matches().filter((match) => !match.locked).length);
  readonly savedCount = computed(() => this.predictionsByMatchId().size);

  ngOnInit(): void {
    forkJoin({
      matches: this.matchService.list(),
      predictions: this.predictionService.mine()
    }).subscribe({
      next: ({ matches, predictions }) => {
        this.matches.set(matches);
        this.predictionsByMatchId.set(new Map(predictions.map((p) => [p.matchId, p])));
        this.state.set('ready');
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load matches.');
        this.state.set('error');
      }
    });
  }

  predictionFor(matchId: number): MatchPrediction | null {
    return this.predictionsByMatchId().get(matchId) ?? null;
  }

  onPredictionSaved(prediction: MatchPrediction): void {
    this.predictionsByMatchId.update((map) => {
      const next = new Map(map);
      next.set(prediction.matchId, prediction);
      return next;
    });
  }
}
