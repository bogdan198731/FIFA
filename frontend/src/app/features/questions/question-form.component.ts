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
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';

import { QuestionService } from '../../core/services/question.service';
import {
  TournamentAnswer,
  TournamentQuestion
} from '../../core/models/tournament-question.model';

type FeedbackKind = 'success' | 'error';

@Component({
  selector: 'app-question-form',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './question-form.component.html',
  styleUrl: './question-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuestionFormComponent implements OnChanges {
  private readonly questionService = inject(QuestionService);

  @Input({ required: true }) question!: TournamentQuestion;
  @Input() savedAnswer: TournamentAnswer | null = null;

  @Output() readonly answerSaved = new EventEmitter<TournamentAnswer>();

  readonly answerControl = new FormControl<string>('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(200)]
  });

  readonly submitting = signal(false);
  readonly feedback = signal<{ kind: FeedbackKind; message: string } | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['savedAnswer'] || changes['question']) {
      this.answerControl.setValue(this.savedAnswer?.answer ?? '');
      if (this.question?.locked) {
        this.answerControl.disable({ emitEvent: false });
      } else {
        this.answerControl.enable({ emitEvent: false });
      }
      this.feedback.set(null);
    }
  }

  save(): void {
    if (this.question.locked || this.submitting()) {
      return;
    }
    if (this.answerControl.invalid) {
      this.answerControl.markAsTouched();
      return;
    }

    const value = this.answerControl.value.trim();
    if (value === (this.savedAnswer?.answer ?? '')) {
      this.feedback.set({ kind: 'success', message: 'No changes to save.' });
      return;
    }

    this.submitting.set(true);
    this.feedback.set(null);

    const obs = this.savedAnswer
      ? this.questionService.update(this.savedAnswer.id, { answer: value })
      : this.questionService.submit({ questionId: this.question.id, answer: value });

    obs.subscribe({
      next: (answer) => {
        this.submitting.set(false);
        this.feedback.set({ kind: 'success', message: 'Saved.' });
        this.answerSaved.emit(answer);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.feedback.set({
          kind: 'error',
          message: err.error?.message ?? 'Could not save your answer.'
        });
      }
    });
  }
}
