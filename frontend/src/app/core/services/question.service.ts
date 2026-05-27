import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  SubmitAnswerRequest,
  TournamentAnswer,
  TournamentQuestion,
  UpdateAnswerRequest
} from '../models/tournament-question.model';

@Injectable({ providedIn: 'root' })
export class QuestionService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/questions`;

  list(): Observable<TournamentQuestion[]> {
    return this.http.get<TournamentQuestion[]>(this.base);
  }

  listMyAnswers(): Observable<TournamentAnswer[]> {
    return this.http.get<TournamentAnswer[]>(`${this.base}/my-answers`);
  }

  submit(req: SubmitAnswerRequest): Observable<TournamentAnswer> {
    return this.http.post<TournamentAnswer>(`${this.base}/answers`, req);
  }

  update(answerId: number, req: UpdateAnswerRequest): Observable<TournamentAnswer> {
    return this.http.put<TournamentAnswer>(`${this.base}/answers/${answerId}`, req);
  }
}
