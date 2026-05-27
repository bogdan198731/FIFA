import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  MatchPrediction,
  PredictionRequest,
  UpdatePredictionRequest
} from '../models/match-prediction.model';

@Injectable({ providedIn: 'root' })
export class PredictionService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/predictions`;

  mine(): Observable<MatchPrediction[]> {
    return this.http.get<MatchPrediction[]>(`${this.base}/my`);
  }

  submit(req: PredictionRequest): Observable<MatchPrediction> {
    return this.http.post<MatchPrediction>(this.base, req);
  }

  update(predictionId: number, req: UpdatePredictionRequest): Observable<MatchPrediction> {
    return this.http.put<MatchPrediction>(`${this.base}/${predictionId}`, req);
  }
}
