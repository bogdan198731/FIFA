import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
  signal
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  Match,
  MatchPrediction
} from '../../core/models/match-prediction.model';
import { I18nService } from '../../core/services/i18n.service';
import { PredictionService } from '../../core/services/prediction.service';
import { teamFlag } from '../../shared/team-flags';

type FeedbackKind = 'success' | 'error';

@Component({
  selector: 'app-match-card',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './match-card.component.html',
  styleUrl: './match-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MatchCardComponent implements OnChanges {
  private readonly predictionService = inject(PredictionService);
  readonly i18n = inject(I18nService);

  @Input({ required: true }) match!: Match;
  @Input() prediction: MatchPrediction | null = null;

  @Output() readonly predictionSaved = new EventEmitter<MatchPrediction>();

  readonly homeScoreControl = new FormControl<number | null>(null, {
    validators: [Validators.required, Validators.min(0)]
  });
  readonly awayScoreControl = new FormControl<number | null>(null, {
    validators: [Validators.required, Validators.min(0)]
  });
  readonly qualifiedTeamControl = new FormControl<string>('', { nonNullable: true });

  readonly submitting = signal(false);
  readonly feedback = signal<{ kind: FeedbackKind; message: string } | null>(null);
  readonly teamFlag = teamFlag;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['prediction'] || changes['match']) {
      this.homeScoreControl.setValue(this.prediction?.homeScore ?? null);
      this.awayScoreControl.setValue(this.prediction?.awayScore ?? null);
      this.qualifiedTeamControl.setValue(this.prediction?.qualifiedTeam ?? '');

      if (this.match?.locked) {
        this.homeScoreControl.disable({ emitEvent: false });
        this.awayScoreControl.disable({ emitEvent: false });
        this.qualifiedTeamControl.disable({ emitEvent: false });
      } else {
        this.homeScoreControl.enable({ emitEvent: false });
        this.awayScoreControl.enable({ emitEvent: false });
        this.qualifiedTeamControl.enable({ emitEvent: false });
      }
      this.feedback.set(null);
    }
  }

  save(): void {
    if (this.match.locked || this.submitting()) {
      return;
    }

    if (this.homeScoreControl.invalid || this.awayScoreControl.invalid) {
      this.homeScoreControl.markAsTouched();
      this.awayScoreControl.markAsTouched();
      return;
    }

    const qualifiedTeam = this.match.matchType === 'KNOCKOUT'
      ? this.qualifiedTeamControl.value
      : null;

    if (this.match.matchType === 'KNOCKOUT' && !qualifiedTeam) {
      this.feedback.set({ kind: 'error', message: 'Choose the team that qualifies.' });
      return;
    }

    const payload = {
      homeScore: this.homeScoreControl.value ?? 0,
      awayScore: this.awayScoreControl.value ?? 0,
      qualifiedTeam
    };

    const sameTeam = (a: string | null | undefined, b: string | null | undefined) =>
      (a ?? '').trim().toLowerCase() === (b ?? '').trim().toLowerCase();

    if (
      this.prediction &&
      payload.homeScore === this.prediction.homeScore &&
      payload.awayScore === this.prediction.awayScore &&
      sameTeam(payload.qualifiedTeam, this.prediction.qualifiedTeam)
    ) {
      this.feedback.set({ kind: 'success', message: 'No changes to save.' });
      return;
    }

    this.submitting.set(true);
    this.feedback.set(null);

    const obs = this.prediction
      ? this.predictionService.update(this.prediction.id, payload)
      : this.predictionService.submit({ matchId: this.match.id, ...payload });

    obs.subscribe({
      next: (prediction) => {
        this.submitting.set(false);
        this.feedback.set({ kind: 'success', message: 'Saved.' });
        this.predictionSaved.emit(prediction);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.feedback.set({
          kind: 'error',
          message: err.error?.message ?? 'Could not save your prediction.'
        });
      }
    });
  }
}
