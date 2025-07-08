import { Routes } from '@angular/router';
import {LoginComponent} from "./main/authentication/login/login.component";
import {SignupComponent} from "./main/authentication/signup/signup.component";
import {DashboardComponent} from "./main/analytics/dashboard/dashboard.component";
import {MenuComponent} from "./shared/menu/menu.component";

const authRoutes: Routes = [
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/signup', component: SignupComponent },
]
const homeRoutes: Routes = [
  { path: 'basic', component: MenuComponent ,
    children: [
      { path: 'dashboard', component: DashboardComponent },
    ]
  },
]
const appRoutes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
]

export const routes: Routes = [
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  ...authRoutes,
  ...homeRoutes,

];
