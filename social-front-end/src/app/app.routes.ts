import { Routes } from '@angular/router';
import {LoginComponent} from "./main/authentication/login/login.component";
import {SignupComponent} from "./main/authentication/signup/signup.component";
import {DashboardComponent} from "./main/analytics/dashboard/dashboard.component";
import {MenuComponent} from "./shared/menu/menu.component";
import {FeedComponent} from "./main/feed/feed.component";
import {InboxComponent} from "./main/inbox/inbox.component";
import {ProductsComponent} from "./main/products/products.component";
import {ContentManagementComponent} from "./main/content-management/content-management.component";
import {AuthGuard} from "./shared/guards/auth.guard";
import {NoAuthGuard} from "./shared/guards/no-auth.guard";
import {MenuClientComponent} from "./client/menu-client/menu-client.component";
import {MarketplaceComponent} from "./client/marketplace/marketplace.component";
import {ProductDetailComponent} from "./client/marketplace/product-detail/product-detail.component";
import {StockListComponent} from "./main/products/stock-list/stock-list.component";
import {ValidatePagesComponent} from "./main/authentication/validate-pages/validate-pages.component";
import {VariantsComponent} from "./main/products/variants/variants.component";

const authRoutes: Routes = [
  { path: 'auth/login', component: LoginComponent, canActivate: [NoAuthGuard] },
  { path: 'auth/signup', component: SignupComponent, canActivate: [NoAuthGuard] },
  { path: 'auth/:platform', component: ValidatePagesComponent },
]
const homeRoutes: Routes = [
  {
    path: 'basic',
    component: MenuComponent,
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'feed', component: FeedComponent },
      { path: 'inbox', component: InboxComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'variants/:idProduct', component: VariantsComponent },
      { path: 'stocks', component: StockListComponent },
      { path: 'content', component: ContentManagementComponent },
    ]
  },
  {
    path: 'client',
    component: MenuClientComponent,
    children: [
      {path: 'marketplace',component: MarketplaceComponent},
      { path: 'marketplace/product/:id', component: ProductDetailComponent }
    ]
  }
]


export const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full'
  },
  ...authRoutes,
  ...homeRoutes,
];
