import { Component } from '@angular/core';
import {BaseChartDirective, NgChartsModule} from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';

@Component({
  selector: 'app-bar-chart',
  standalone: true,
  imports: [
    NgChartsModule
  ],
  templateUrl: './bar-chart.component.html',
  styleUrl: './bar-chart.component.css'
})
export class BarChartComponent {
  public barChartType: ChartType = 'bar';
  public barChartLegend = false;

  private zinc500 = '#cac9ca';

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Q1','Q2','Q3','Q4'],
    datasets: [{
      data: [65, 42, 88, 31],
      label: 'Sales',
      backgroundColor: this.zinc500,
      borderColor: this.zinc500,
      borderWidth: 1,
      borderRadius:13
    }]
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: {
      x: { display: false },
      y: { display: false }
    }
  };
}
