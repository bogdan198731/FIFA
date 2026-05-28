import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Attaches the JWT to outgoing API calls and signs the user out on a 401.
 *
 * - The Authorization header is only added when the request targets our own
 *   API (so the token never leaks to third-party hosts).
 * - On 401, we delegate the navigation to AuthService.logout(); it already
 *   clears the session and routes to /login. Doing it twice was racing the
 *   router during the bootstrap /auth/me call.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  const token = auth.getToken();
  const isApiRequest =
    req.url.startsWith(environment.apiBaseUrl) || req.url.startsWith('/api');

  const request = token && isApiRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && token && isApiRequest) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
