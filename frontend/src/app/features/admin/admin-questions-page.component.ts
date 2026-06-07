import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { environment } from '../../../environments/environment';
import { TournamentQuestion, OptionSource } from '../../core/models/tournament-question.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-questions-page',
  standalone: true,
  imports: [DatePipe, LoadingSpinnerComponent, EmptyStateComponent],
  templateUrl: './admin-questions-page.component.html',
  styleUrls: ['./admin-page.component.scss', './admin-questions-page.component.scss']
})
export class AdminQuestionsPageComponent implements OnInit {
  private readonly http = inject(HttpClient);

  readonly questions = signal<TournamentQuestion[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);
  readonly saving = signal<number | null>(null);
  readonly feedback = signal<{ id: number; msg: string; ok: boolean } | null>(null);

  readonly sourceOptions: { value: OptionSource; label: string; description: string }[] = [
    { value: 'STATIC',  label: 'Static list',  description: 'Options stored as a fixed list' },
    { value: 'TEAMS',   label: 'All teams',    description: 'Resolved live from all teams in matches' },
    { value: 'PLAYERS', label: 'All players',  description: 'Resolved live from the players table' },
  ];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.errorMessage.set(null);
    this.http.get<TournamentQuestion[]>(`${environment.apiBaseUrl}/questions`).subscribe({
      next: questions => {
        this.questions.set(questions);
        this.state.set('ready');
      },
      error: err => {
        this.errorMessage.set(err.error?.message ?? 'Could not load tournament questions.');
        this.state.set('error');
      }
    });
  }

  updateSource(question: TournamentQuestion, source: OptionSource): void {
    if (source === question.optionSource) return;
    this.saving.set(question.id);
    this.feedback.set(null);

    this.http.patch<TournamentQuestion>(
      `${environment.apiBaseUrl}/admin/questions/${question.id}/option-source`,
      { optionSource: source }
    ).subscribe({
      next: updated => {
        this.questions.update(list => list.map(q => q.id === updated.id ? updated : q));
        this.saving.set(null);
        this.feedback.set({ id: question.id, msg: 'Saved', ok: true });
      },
      error: err => {
        this.saving.set(null);
        this.feedback.set({ id: question.id, msg: err.error?.message ?? 'Error', ok: false });
      }
    });
  }

  sourceLabel(source: OptionSource): string {
    return this.sourceOptions.find(s => s.value === source)?.label ?? source;
  }
}
