import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {NgChartsModule} from "ng2-charts";
import {ChartConfiguration, ChartType} from "chart.js";
import {PagesRepartitionDto} from "../dashboard.service";

@Component({
  selector: 'app-account-chart',
  standalone: true,
  imports: [
    NgChartsModule
  ],
  templateUrl: './account-chart.component.html',
  styleUrl: './account-chart.component.css'
})
export class AccountChartComponent implements OnChanges {
  @Input() data: PagesRepartitionDto[] = [];

  public barChartType: ChartType = 'bar';
  public barChartLegend = false;
  private zinc500 = '#cac9ca';

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Deals',
      backgroundColor: this.zinc500,
      borderColor: this.zinc500,
      borderWidth: 1,
      borderRadius: 13
    }]
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y', // horizontal bars
    plugins: { legend: { display: false } },
    scales: {
      x: { display: false },
      y: { display: true } // we keep y visible to show page names
    }
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.updateChart();
    }
  }

  private updateChart(): void {
    this.barChartData = {
      labels: this.data.map(d => d.pageTitle),
      datasets: [{
        data: this.data.map(d => d.totalPercentage),
        label: 'Deals %',
        backgroundColor: this.zinc500,
        borderColor: this.zinc500,
        borderWidth: 1,
        borderRadius: 13
      }]
    };
  }
}
