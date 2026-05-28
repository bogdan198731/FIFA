import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { QuestionFormComponent } from './question-form.component';
import { QuestionService } from '../../core/services/question.service';
import {
  TournamentAnswer,
  TournamentQuestion
} from '../../core/models/tournament-question.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-tournament-questions-page',
  standalone: true,
  imports: [EmptyStateComponent, LoadingSpinnerComponent, QuestionFormComponent],
  templateUrl: './tournament-questions-page.component.html',
  styleUrl: './tournament-questions-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentQuestionsPageComponent implements OnInit {
  private readonly questionService = inject(QuestionService);

  readonly questions = signal<TournamentQuestion[]>([]);
  readonly answersByQuestionId = signal<Map<number, TournamentAnswer>>(new Map());
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  readonly openCount = computed(() =>
    this.questions().filter((q) => !q.locked).length
  );

  ngOnInit(): void {
    forkJoin({
      questions: this.questionService.list(),
      answers: this.questionService.listMyAnswers()
    }).subscribe({
      next: ({ questions, answers }) => {
        this.questions.set(questions);
        this.answersByQuestionId.set(new Map(answers.map((a) => [a.questionId, a])));
        this.state.set('ready');
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Could not load questions.');
        this.state.set('error');
      }
    });
  }

  answerFor(questionId: number): TournamentAnswer | null {
    return this.answersByQuestionId().get(questionId) ?? null;
  }

  onAnswerSaved(answer: TournamentAnswer): void {
    this.answersByQuestionId.update((map) => {
      const next = new Map(map);
      next.set(answer.questionId, answer);
      return next;
    });
  }
}
