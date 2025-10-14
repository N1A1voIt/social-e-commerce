import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import {SalesProgressionDto} from "../dashboard.service";

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [NgChartsModule],
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.css']
})
export class LineChartComponent implements  OnInit,OnChanges {
  public lineChartType: 'line' = 'line';
  public lineChartLegend = false;
  @Input() data!:SalesProgressionDto;
  private zinc500 = '#6b7280';

  // Simulated backend data (replace later with your real API call)
  // private salesData = this.data.labels;

  public lineChartData: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  public lineChartOptions: ChartConfiguration<'line'>['options'] = {};
  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) {
      const labels = this.data.labels.map(d => {
        const date = new Date(d);
        return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short' }); // "10 Oct"
      });

      const values = this.data.data;

      this.lineChartData = {
        labels,
        datasets: [
          {
            data: values,
            label: 'Total Sales',
            fill: true,
            tension: 0.4,
            borderColor: this.zinc500,
            backgroundColor: 'rgba(107, 114, 128, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          }
        ]
      };

      this.lineChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: {
            ticks: { color: '#9ca3af' },
            grid: { display: false },
          },
          y: {
            ticks: { color: '#9ca3af' },
            grid: { color: '#e5e7eb' },
          }
        },
        elements: { line: { tension: 0.4 } }
      };
    }
  }

  ngOnInit(): void {
    if (this.data) {
      const labels = this.data.labels.map(d => {
        const date = new Date(d);
        return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short' }); // "10 Oct"
      });
      const values = this.data.data;

      this.lineChartData = {
        labels,
        datasets: [
          {
            data: values,
            label: 'Total Sales',
            fill: true,
            tension: 0.4,
            borderColor: this.zinc500,
            backgroundColor: 'rgba(107, 114, 128, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          }
        ]
      };

      this.lineChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: {
            ticks: { color: '#9ca3af' },
            grid: { display: false },
          },
          y: {
            ticks: { color: '#9ca3af' },
            grid: { color: '#e5e7eb' },
          }
        },
        elements: { line: { tension: 0.4 } }
      };
    }
  }

}
