import { Component, Input, OnChanges } from '@angular/core';
import { ChartConfiguration } from 'chart.js/auto';
import { ChartType } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';

@Component({
  selector: 'app-chart-display',
  templateUrl: 'chart-display.component.html',
  standalone: true,
  imports: [
    NgChartsModule
  ],
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    canvas {
      max-width: 100%;
    }
  `]
})
export class ChartDisplayComponent implements  OnChanges {
  @Input() chart: any;

  public chartType: ChartType = 'line';
  public chartData: ChartConfiguration['data'] = { labels: [], datasets: [] };
  public chartOptions: ChartConfiguration['options'] = {};

  private zinc500 = '#cac9ca';

  ngOnChanges(): void {
    if (this.chart) {
      this.loadChart();
      console.log(this.chartData)
    }
  }

  private loadChart(): void {
    const type = this.chart?.type || 'line';
    const x = this.chart?.data?.x || [];
    const y = this.chart?.data?.y || [];

    this.chartType = type as ChartType;

    this.chartData = {
      labels: x,
      datasets: [
        {
          data: y,
          label: 'AI Data',
          backgroundColor: this.zinc500,
          borderColor: this.zinc500,
          borderWidth: 1,
          borderRadius: 10,
          fill: false
        }
      ]
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { display: true },
        y: { display: true }
      }
    };
  }
}
