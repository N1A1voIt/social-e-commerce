import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
// import {provideCharts, withDefaultRegisterables} from "ng2-charts";
import { provideAuth,getAuth } from '@angular/fire/auth';
import { environment } from '../environments/environment';
import { provideFirebaseApp, initializeApp } from '@angular/fire/app';
import {provideHttpClient} from "@angular/common/http";

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes),provideHttpClient(),provideFirebaseApp(() => initializeApp(environment.firebase)),
    provideAuth(() => getAuth()),]
};
