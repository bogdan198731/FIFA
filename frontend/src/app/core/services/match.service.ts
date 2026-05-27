import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Match } from '../models/match-prediction.model';

@Injectable({ providedIn: 'root' })
export class MatchService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/matches`;

  list(): Observable<Match[]> {
    return this.http.get<Match[]>(this.base);
  }

  upcoming(): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.base}/upcoming`);
  }
}
