import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, map } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NoAuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.isLoggedIn().pipe(
      map(isLoggedIn => {
        if (!isLoggedIn) {
          return true;
        } else {
          return this.router.createUrlTree(['/basic/dashboard']);
        }
      })
    );
  }
}
