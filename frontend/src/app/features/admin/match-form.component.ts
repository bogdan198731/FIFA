import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AdminMatchRequest, Match } from '../../core/models/match-prediction.model';
import { AdminMatchService } from '../../core/services/admin-match.service';
import { fromDateTimeLocal, matchStages, matchTypes, toDateTimeLocal } from './admin-utils';

@Component({
  selector: 'app-match-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './match-form.component.html',
  styleUrl: './admin-form.component.scss'
})
export class MatchFormComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly adminMatchService = inject(AdminMatchService);

  @Input() match: Match | null = null;
  @Output() readonly saved = new EventEmitter<Match>();
  @Output() readonly deleted = new EventEmitter<number>();

  readonly stages = matchStages;
  readonly types = matchTypes;
  readonly submitting = signal(false);
  readonly feedback = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    homeTeam: ['', [Validators.required, Validators.maxLength(64)]],
    awayTeam: ['', [Validators.required, Validators.maxLength(64)]],
    kickoffTime: ['', Validators.required],
    venue: [''],
    stage: ['GROUP', Validators.required],
    groupName: ['', Validators.maxLength(16)],
    matchType: ['REGULAR', Validators.required],
    isFinished: [false],
    homeScore: [null as number | null, Validators.min(0)],
    awayScore: [null as number | null, Validators.min(0)],
    qualifiedTeam: ['']
  });

  /** Group is only relevant (and required) for group-stage matches. */
  get isGroupStage(): boolean {
    return this.form.controls.stage.value === 'GROUP';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['match']) {
      this.feedback.set(null);
      if (!this.match) {
        this.form.reset({
          homeTeam: '',
          awayTeam: '',
          kickoffTime: '',
          venue: '',
          stage: 'GROUP',
          groupName: '',
          matchType: 'REGULAR',
          isFinished: false,
          homeScore: null,
          awayScore: null,
          qualifiedTeam: ''
        });
        return;
      }
      this.form.reset({
        homeTeam: this.match.homeTeam,
        awayTeam: this.match.awayTeam,
        kickoffTime: toDateTimeLocal(this.match.kickoffAt),
        venue: this.match.venue ?? '',
        stage: this.match.stage,
        groupName: this.match.groupName ?? '',
        matchType: this.match.matchType,
        isFinished: this.match.finished,
        homeScore: this.match.homeScore,
        awayScore: this.match.awayScore,
        qualifiedTeam: this.match.knockoutWinner ?? ''
      });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const req: AdminMatchRequest = {
      homeTeam: raw.homeTeam,
      awayTeam: raw.awayTeam,
      kickoffTime: fromDateTimeLocal(raw.kickoffTime),
      venue: raw.venue || null,
      stage: raw.stage as AdminMatchRequest['stage'],
      groupName: raw.stage === 'GROUP' ? (raw.groupName.trim() || null) : null,
      matchType: raw.matchType as AdminMatchRequest['matchType'],
      isFinished: raw.isFinished,
      homeScore: raw.homeScore,
      awayScore: raw.awayScore,
      qualifiedTeam: raw.qualifiedTeam || null
    };

    this.submitting.set(true);
    this.feedback.set(null);
    const obs = this.match
      ? this.adminMatchService.update(this.match.id, req)
      : this.adminMatchService.create(req);

    obs.subscribe({
      next: (match) => {
        this.submitting.set(false);
        this.feedback.set('Saved.');
        this.saved.emit(match);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.feedback.set(err.error?.message ?? 'Could not save match.');
      }
    });
  }

  remove(): void {
    if (!this.match || !confirm(`Delete ${this.match.homeTeam} vs ${this.match.awayTeam}?`)) {
      return;
    }
    this.adminMatchService.delete(this.match.id).subscribe({
      next: () => this.deleted.emit(this.match!.id),
      error: (err: HttpErrorResponse) => this.feedback.set(err.error?.message ?? 'Could not delete match.')
    });
  }
}
