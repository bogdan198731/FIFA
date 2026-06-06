import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { environment } from '../../../environments/environment';
import { TournamentQuestion, OptionSource } from '../../core/models/tournament-question.model';

@Component({
  selector: 'app-admin-questions-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-questions-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminQuestionsPageComponent implements OnInit {
  private readonly http = inject(HttpClient);

  readonly questions = signal<TournamentQuestion[]>([]);
  readonly saving = signal<number | null>(null);
  readonly feedback = signal<{ id: number; msg: string; ok: boolean } | null>(null);

  readonly sourceOptions: { value: OptionSource; label: string; description: string }[] = [
    { value: 'STATIC',  label: 'Static list',  description: 'Options stored as a fixed list' },
    { value: 'TEAMS',   label: 'All teams',    description: 'Resolved live from all teams in matches' },
    { value: 'PLAYERS', label: 'All players',  description: 'Resolved live from the players table' },
  ];

  ngOnInit(): void {
    this.http.get<TournamentQuestion[]>(`${environment.apiBaseUrl}/questions`)
      .subscribe(q => this.questions.set(q));
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
