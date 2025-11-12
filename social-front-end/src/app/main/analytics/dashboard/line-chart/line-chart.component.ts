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
  public lineChartLegend = true;
  @Input() data!:SalesProgressionDto;
  private zinc500 = '#6b7280';
  private blue500 = '#3b82f6';
  private green500 = '#22c55e';

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
      // Create a second dataset (e.g., average or projected values)
      const secondValues = this.data.paidAmounts; // Example: 80% of actual sales

      this.lineChartData = {
        labels,
        datasets: [
          {
            data: values,
            label: 'Paid amount',
            fill: true,
            tension: 0.4,
            borderColor: this.blue500,
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          },
          {
            data: secondValues,
            label: 'Total amount',
            fill: true,
            tension: 0.4,
            borderColor: this.green500,
            backgroundColor: 'rgba(34, 197, 94, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          }
        ]
      };

      this.lineChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              color: '#6b7280',
              font: { size: 12 },
              usePointStyle: true,
              padding: 15
            }
          }
        },
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
      // Create a second dataset (e.g., average or projected values)
      const secondValues = values.map(v => v * 0.8); // Example: 80% of actual sales

      this.lineChartData = {
        labels,
        datasets: [
          {
            data: values,
            label: 'Actual Sales',
            fill: true,
            tension: 0.4,
            borderColor: this.blue500,
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          },
          {
            data: secondValues,
            label: 'Target Sales',
            fill: true,
            tension: 0.4,
            borderColor: this.green500,
            backgroundColor: 'rgba(34, 197, 94, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 5,
          }
        ]
      };

      this.lineChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              color: '#6b7280',
              font: { size: 12 },
              usePointStyle: true,
              padding: 15
            }
          }
        },
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

