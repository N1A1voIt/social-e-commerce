import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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
import {DashboardService, DashboardStats} from "./dashboard.service";
import {AccountChartComponent} from "./account-chart/account-chart.component";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    PlatformBadgeComponent,
    LinkComponent,
    ActionBadgeComponent,
    NgIcon,
    RevenueBadgeComponent,
    TopSalesCardComponent,
    BestDealsCardComponent,
    DescriptiveCardComponent,
    AccountPerformancesComponent,
    BarChartComponent,
    AccountChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  dashboardStats: DashboardStats | null = null;
  loading = true;
  error: string | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.loading = true;
    this.error = null;

    this.dashboardService.getDashboardStats().subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.dashboardStats = response.data;
        } else {
          this.error = 'Failed to load dashboard data';
        }
        this.loading = false;
        console.log(this.dashboardStats)
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.error = 'Error loading dashboard data';
        this.loading = false;
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  }

  formatNumber(num: number): string {
    return new Intl.NumberFormat('en-US').format(num);
  }
}
