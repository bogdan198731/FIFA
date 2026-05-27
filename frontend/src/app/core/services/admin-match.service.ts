import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AdminMatchRequest,
  Match,
  MatchResultRequest
} from '../models/match-prediction.model';

@Injectable({ providedIn: 'root' })
export class AdminMatchService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/admin/matches`;

  create(req: AdminMatchRequest): Observable<Match> {
    return this.http.post<Match>(this.base, req);
  }

  update(matchId: number, req: AdminMatchRequest): Observable<Match> {
    return this.http.put<Match>(`${this.base}/${matchId}`, req);
  }

  delete(matchId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${matchId}`);
  }

  updateResult(matchId: number, req: MatchResultRequest): Observable<Match> {
    return this.http.put<Match>(`${this.base}/${matchId}/result`, req);
  }

  lock(matchId: number): Observable<Match> {
    return this.http.put<Match>(`${this.base}/${matchId}/lock`, {});
  }

  unlock(matchId: number): Observable<Match> {
    return this.http.put<Match>(`${this.base}/${matchId}/unlock`, {});
  }
}
