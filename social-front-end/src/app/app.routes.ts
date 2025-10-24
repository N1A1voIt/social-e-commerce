import { Routes } from '@angular/router';
import {LoginComponent} from "./main/authentication/login/login.component";
import {SignupComponent} from "./main/authentication/signup/signup.component";
import {DashboardComponent} from "./main/analytics/dashboard/dashboard.component";
import {MenuComponent} from "./shared/menu/menu.component";
import {FeedComponent} from "./main/feed/feed.component";
import {InboxComponent} from "./main/inbox/inbox.component";
import {ProductsComponent} from "./main/products/products.component";
import {ContentManagementComponent} from "./main/content-management/content-management.component";
import {PostDetailsComponent} from "./main/content-management/post-details/post-details.component";
import {AuthGuard} from "./shared/guards/auth.guard";
import {NoAuthGuard} from "./shared/guards/no-auth.guard";
import {MenuClientComponent} from "./client/menu-client/menu-client.component";
import {MarketplaceComponent} from "./client/marketplace/marketplace.component";
import {ProductDetailComponent} from "./client/marketplace/product-detail/product-detail.component";
import {CartComponent} from "./client/cart/cart.component";
import {StockListComponent} from "./main/products/stock-list/stock-list.component";
import {ValidatePagesComponent} from "./main/authentication/validate-pages/validate-pages.component";
import {VariantsComponent} from "./main/products/variants/variants.component";
import {OrdersComponent} from "./main/orders/orders.component";
import {TransactionComponent} from "./main/transaction/transaction.component";
import {SuccessRedirectionComponent} from "./client/success-redirection/success-redirection.component";
import {DsignupComponent} from "./deliverer/authentication/dsignup/dsignup.component";
import {DsigninComponent} from "./deliverer/authentication/dsignin/dsignin.component";
import {MenuDeliveryComponent} from "./deliverer/main/menu-delivery/menu-delivery.component";
import {DdashboardComponent} from "./deliverer/main/ddashboard/ddashboard.component";
import {DeliveryAuthGuard} from "./shared/guards/delivery-auth.guard";
import {PreviousMissionComponent} from "./deliverer/main/previous-mission/previous-mission.component";
import {PendingRequestComponent} from "./deliverer/main/pending-request/pending-request.component";
import {ClientLoginComponent} from "./client/authentication/client-login/client-login.component";
import {ClientSignupComponent} from "./client/authentication/client-signup/client-signup.component";

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
      { path: 'content/post/:id', component: PostDetailsComponent },
      { path: 'orders', component: OrdersComponent },
    ]
  },

  {
    path: 'client',
    component: MenuClientComponent,
    children: [
      {path: 'marketplace',component: MarketplaceComponent},
      { path: 'marketplace/product/:id', component: ProductDetailComponent },
      { path: 'cart', component: CartComponent }
    ]
  },
]

const freeRoutes:Routes = [
  {path:'transactions',component:TransactionComponent},
  {path:'success-redirection',component:SuccessRedirectionComponent}
]

const deliveryRoutes = [
  {
    path:'delivery/signup',
    component:DsignupComponent
  } , {
    path:'delivery/signin',
    component:DsigninComponent
  },
  {
    path: 'delivery/space',
    component:MenuDeliveryComponent,
    canActivate:[DeliveryAuthGuard],
    canActivateChild:[DeliveryAuthGuard],
    children: [
      {path: 'dashboard',component:DdashboardComponent},
      {path: 'mission-history',component:PreviousMissionComponent},
      {path: 'pending-applications',component:PendingRequestComponent}
    ]
  }
]
export const clientAuthRoutes : Routes = [
  {
    path: 'client/auth',
    children: [
      { path: 'login',component: ClientLoginComponent },
      { path: 'signup', component: ClientSignupComponent }
    ]
  },
]
export const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full'
  },
  ...authRoutes,
  ...homeRoutes,
  ...freeRoutes,
  ...deliveryRoutes,
  ...clientAuthRoutes
];
