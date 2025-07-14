import { Routes } from '@angular/router';
import {LoginComponent} from "./main/authentication/login/login.component";
import {SignupComponent} from "./main/authentication/signup/signup.component";
import {DashboardComponent} from "./main/analytics/dashboard/dashboard.component";
import {MenuComponent} from "./shared/menu/menu.component";
import {FeedComponent} from "./main/feed/feed.component";
import {InboxComponent} from "./main/inbox/inbox.component";
import {ProductsComponent} from "./main/products/products.component";
import {ContentManagementComponent} from "./main/content-management/content-management.component";
import {DeliveryManagementComponent} from "./main/delivery-management/delivery-management.component";

const authRoutes: Routes = [
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/signup', component: SignupComponent },
]
const homeRoutes: Routes = [
  { path: 'basic', component: MenuComponent ,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'feed', component: FeedComponent },
      { path: 'inbox', component: InboxComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'content', component: ContentManagementComponent },
      { path: 'delivery', component: DeliveryManagementComponent },
    ]
  },
]

export const routes: Routes = [
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  ...authRoutes,
  ...homeRoutes,

];
