import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, of, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse
} from '../models/auth.model';

const TOKEN_KEY = 'worldcup.token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _currentUser = signal<UserResponse | null>(null);
  private readonly _initialized = signal(false);

  readonly currentUser = this._currentUser.asReadonly();
  readonly initialized = this._initialized.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly isAdmin = computed(() => this._currentUser()?.role === 'ADMIN');

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /** Called once at startup; hydrates the current user if a token is stored. */
  bootstrap(): void {
    if (!this.getToken()) {
      this._initialized.set(true);
      return;
    }
    this.http.get<UserResponse>(`${environment.apiBaseUrl}/auth/me`).subscribe({
      next: (user) => {
        this._currentUser.set(user);
        this._initialized.set(true);
      },
      error: () => {
        this.clearSession();
        this._initialized.set(true);
      }
    });
  }

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, req)
      .pipe(tap((res) => this.applySession(res)));
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, req)
      .pipe(tap((res) => this.applySession(res)));
  }

  logout(): void {
    this.clearSession();
    this.router.navigateByUrl('/login');
  }

  refreshCurrentUser(): Observable<UserResponse | null> {
    return this.http.get<UserResponse>(`${environment.apiBaseUrl}/auth/me`).pipe(
      tap((user) => this._currentUser.set(user)),
      catchError(() => {
        this.clearSession();
        return of(null);
      })
    );
  }

  private applySession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    this._currentUser.set(res.user);
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._currentUser.set(null);
  }
}
