import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ScoreRecalculationResponse } from '../models/match-prediction.model';

@Injectable({ providedIn: 'root' })
export class AdminScoreService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/admin/scores`;

  recalculateAll(): Observable<ScoreRecalculationResponse> {
    return this.http.post<ScoreRecalculationResponse>(`${this.base}/recalculate`, {});
  }

  recalculateMatch(matchId: number): Observable<ScoreRecalculationResponse> {
    return this.http.post<ScoreRecalculationResponse>(
      `${this.base}/recalculate/matches/${matchId}`,
      {}
    );
  }
}
