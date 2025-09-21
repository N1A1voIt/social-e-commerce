import {CanActivate, CanActivateChild, CanActivateFn, Router, UrlTree} from '@angular/router';
import {map, Observable} from "rxjs";
import {DeliveryAuthService} from "../services/delivery-auth.service";
import {Injectable} from "@angular/core";
@Injectable({
  providedIn: 'root'
})
export class DeliveryAuthGuard implements CanActivate, CanActivateChild {
  constructor(private authService: DeliveryAuthService, private router: Router) {
  }

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.isLoggedIn().pipe(
      map(isLoggedIn => {
        if (isLoggedIn) {
          return true;
        } else {
          return this.router.createUrlTree(['/auth/login']);
        }
      })
    );
  }

  canActivateChild(): Observable<boolean | UrlTree> {
    return this.canActivate();
  }
}
