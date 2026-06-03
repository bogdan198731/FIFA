import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { MatchCardComponent } from './match-card.component';
import {
  Match,
  MatchStage,
  MatchPrediction
} from '../../core/models/match-prediction.model';
import { MatchService } from '../../core/services/match.service';
import { PredictionService } from '../../core/services/prediction.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

type LoadState = 'loading' | 'ready' | 'error';

/** A foldable section of matches — one per group, then one per knockout stage. */
interface MatchSection {
  key: string;
  title: string;
  order: number;
  groupLabel: string;
  matches: Match[];
}

const STAGE_ORDER: Record<MatchStage, number> = {
  GROUP: 0,
  ROUND_OF_32: 1,
  ROUND_OF_16: 2,
  QUARTER_FINAL: 3,
  SEMI_FINAL: 4,
  THIRD_PLACE: 5,
  FINAL: 6
};

const STAGE_LABEL: Record<MatchStage, string> = {
  GROUP: 'Group stage',
  ROUND_OF_32: 'Round of 32',
  ROUND_OF_16: 'Round of 16',
  QUARTER_FINAL: 'Quarter-finals',
  SEMI_FINAL: 'Semi-finals',
  THIRD_PLACE: 'Third place',
  FINAL: 'Final'
};

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

  /**
   * Matches bucketed into foldable sections: one per group (group stage),
   * then one per knockout stage. Sections are ordered group A→… then by stage;
   * matches within a section are ordered by kickoff.
   */
  readonly sections = computed<MatchSection[]>(() => {
    const ordered = [...this.matches()].sort((a, b) =>
      a.kickoffAt.localeCompare(b.kickoffAt)
    );

    const byKey = new Map<string, MatchSection>();
    for (const match of ordered) {
      const isGroup = match.stage === 'GROUP';
      const groupLabel = isGroup ? (match.groupName ?? '') : '';
      const key = isGroup ? `group:${groupLabel || 'none'}` : `stage:${match.stage}`;
      const title = isGroup
        ? (match.groupName ? `Group ${match.groupName}` : 'Group stage')
        : STAGE_LABEL[match.stage];

      let section = byKey.get(key);
      if (!section) {
        section = {
          key,
          title,
          order: STAGE_ORDER[match.stage],
          groupLabel,
          matches: []
        };
        byKey.set(key, section);
      }
      section.matches.push(match);
    }

    return [...byKey.values()].sort(
      (a, b) =>
        a.order - b.order ||
        a.groupLabel.localeCompare(b.groupLabel) ||
        a.title.localeCompare(b.title)
    );
  });

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

  /** Number of still-open (unlocked) matches in a section, for the summary. */
  openInSection(section: MatchSection): number {
    return section.matches.filter((match) => !match.locked).length;
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
