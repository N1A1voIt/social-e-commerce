import { Component } from '@angular/core';
import {PlatformBadgeComponent} from "./platform-badge/platform-badge.component";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PlatformBadgeComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

}
