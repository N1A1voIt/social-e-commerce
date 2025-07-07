import { Routes } from '@angular/router';
import {LoginComponent} from "./main/authentication/login/login.component";
import {SignupComponent} from "./main/authentication/signup/signup.component";

const authRoutes: Routes = [
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/signup', component: SignupComponent },
]
const appRoutes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
]

export const routes: Routes = [
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  ...authRoutes,

];
