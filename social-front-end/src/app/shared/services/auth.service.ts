import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { TokenDTO, TokenValidationResponse } from '../models/token.model';
import { javaHost } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  validateToken(token: string): Observable<boolean> {
    if (!token) {
      return of(false);
    }

    const tokenDTO: TokenDTO = { token };

    return this.http.post<TokenValidationResponse>(`${javaHost}/api/auth/validate-token`, tokenDTO)
      .pipe(
        map(response => response.data.valid),
        catchError(() => of(false))
      );
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): Observable<boolean> {
    const token = this.getToken();
    if (!token) {
      return of(false);
    }
    return this.validateToken(token);
  }
}
