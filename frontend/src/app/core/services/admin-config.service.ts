import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AdminConfig } from '../models/admin-user.model';

@Injectable({ providedIn: 'root' })
export class AdminConfigService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/admin/config`;

  get(): Observable<AdminConfig> {
    return this.http.get<AdminConfig>(this.base);
  }
}
