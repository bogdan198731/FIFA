import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { SyncResult } from '../models/sync.model';

/**
 * Triggers the backend's external-API sync agent (API-Football), which
 * fetches fixtures/results and upserts them into the database.
 */
@Injectable({ providedIn: 'root' })
export class SyncService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/admin/sync`;

  /** POST /api/admin/sync — pulls fresh data from the external API. */
  run(): Observable<SyncResult> {
    return this.http.post<SyncResult>(this.base, {});
  }
}
