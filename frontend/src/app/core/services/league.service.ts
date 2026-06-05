import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PrivateLeague } from '../models/league.model';
import { LeaderboardEntry } from '../models/leaderboard.model';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/leagues`;

  myLeagues(): Observable<PrivateLeague[]> {
    return this.http.get<PrivateLeague[]>(this.base);
  }

  create(name: string): Observable<PrivateLeague> {
    return this.http.post<PrivateLeague>(this.base, { name });
  }

  join(inviteCode: string): Observable<PrivateLeague> {
    return this.http.post<PrivateLeague>(`${this.base}/join`, { inviteCode });
  }

  leaderboard(leagueId: number): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.base}/${leagueId}/leaderboard`);
  }
}
