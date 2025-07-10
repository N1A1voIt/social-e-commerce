import { Component } from '@angular/core';
import {PlatformBadgeComponent} from "./platform-badge/platform-badge.component";
import {LinkComponent} from "../../../shared/menu/sidebar/link/link.component";
import {ActionBadgeComponent} from "./action-badge/action-badge.component";
import {NgIcon} from "@ng-icons/core";
import {RevenueBadgeComponent} from "./revenue-badge/revenue-badge.component";
import {TopSalesCardComponent} from "./top-sales-card/top-sales-card.component";
import {BestDealsCardComponent} from "./best-deals-card/best-deals-card.component";
import {DescriptiveCardComponent} from "./descriptive-card/descriptive-card.component";
import {AccountPerformancesComponent} from "./account-performances/account-performances.component";
import {BarChartComponent} from "./bar-chart/bar-chart.component";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PlatformBadgeComponent,
    LinkComponent,
    ActionBadgeComponent,
    NgIcon,
    RevenueBadgeComponent,
    TopSalesCardComponent,
    BestDealsCardComponent,
    DescriptiveCardComponent,
    AccountPerformancesComponent,
    BarChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

}
