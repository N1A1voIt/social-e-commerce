import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideCharts, withDefaultRegisterables} from "ng2-charts";
import { provideAuth, getAuth } from '@angular/fire/auth';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideCharts(withDefaultRegisterables()),provideFirebaseApp(() => initializeApp(environment.firebase)),
    provideAuth(() => getAuth()),]
};
