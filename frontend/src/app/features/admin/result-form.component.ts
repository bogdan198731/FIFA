import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { Match, MatchResultRequest } from '../../core/models/match-prediction.model';
import { AdminMatchService } from '../../core/services/admin-match.service';
import { lockLabel } from './admin-utils';

@Component({
  selector: 'app-result-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './result-form.component.html',
  styleUrl: './admin-form.component.scss'
})
export class ResultFormComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly adminMatchService = inject(AdminMatchService);

  @Input({ required: true }) match!: Match;
  @Output() readonly changed = new EventEmitter<Match>();

  readonly submitting = signal(false);
  readonly feedback = signal<string | null>(null);
  readonly lockLabel = lockLabel;

  readonly form = this.fb.nonNullable.group({
    isFinished: [false],
    homeScore: [null as number | null, Validators.min(0)],
    awayScore: [null as number | null, Validators.min(0)],
    qualifiedTeam: ['']
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['match']) {
      this.feedback.set(null);
      this.form.reset({
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
    const req: MatchResultRequest = {
      isFinished: raw.isFinished,
      homeScore: raw.homeScore,
      awayScore: raw.awayScore,
      qualifiedTeam: raw.qualifiedTeam || null
    };

    this.submitting.set(true);
    this.feedback.set(null);
    this.adminMatchService.updateResult(this.match.id, req).subscribe({
      next: (match) => {
        this.submitting.set(false);
        this.feedback.set('Saved.');
        this.changed.emit(match);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.feedback.set(err.error?.message ?? 'Could not save result.');
      }
    });
  }

  lock(): void {
    this.adminMatchService.lock(this.match.id).subscribe((match) => this.changed.emit(match));
  }

  unlock(): void {
    this.adminMatchService.unlock(this.match.id).subscribe((match) => this.changed.emit(match));
  }
}
