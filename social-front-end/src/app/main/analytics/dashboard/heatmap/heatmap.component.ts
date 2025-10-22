import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix';
import { Chart } from 'chart.js';
import {HeatmapData} from "../dashboard.service";

Chart.register(MatrixController, MatrixElement);

@Component({
  selector: 'app-heatmap-chart',
  standalone: true,
  imports: [NgChartsModule],
  // The component's HTML must be *just* the canvas
  templateUrl: 'heatmap.component.html',
  // Add styles for the canvas to fill its parent
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
  `]
})
export class HeatmapComponent implements OnInit, OnChanges {
  public heatmapChartType: 'matrix' = 'matrix';
  @Input() data!: HeatmapData;
  @Input() metricType: 'postCount' | 'avgReactions' = 'postCount';

  public heatmapChartData: ChartConfiguration<'matrix'>['data'] = { datasets: [] };
  public heatmapChartOptions: ChartConfiguration<'matrix'>['options'] = {};

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) this.updateHeatmap();
    if (changes['metricType']) this.updateHeatmap();
  }

  ngOnInit(): void {
    if (this.data) this.updateHeatmap();
  }

  private updateHeatmap() {
    if (!this.data || !this.data.cells || this.data.cells.length === 0) {
      this.heatmapChartData = { datasets: [] };
      return;
    }

    const { xlabels, ylabels, cells, timeFrame } = this.data;

    // --- Min/Max calculation remains the same ---
    const values = cells.map(cell =>
      this.metricType === 'postCount' ? cell.postCount : cell.avgReactions
    );
    const nonZeroValues = values.filter(v => v > 0);
    const maxValue = nonZeroValues.length > 0 ? Math.max(...nonZeroValues) : 1;
    const minValue = nonZeroValues.length > 0 ? Math.min(...nonZeroValues) : 0;
    // --- End Min/Max ---

    const matrixData = cells.map(cell => ({
      x: cell.x,
      y: cell.y,
      v: this.metricType === 'postCount' ? cell.postCount : cell.avgReactions,
      postCount: cell.postCount,
      avgReactions: cell.avgReactions
    }));

    this.heatmapChartData = {
      datasets: [
        {
          label: 'Activity',
          data: matrixData as any,
          // --- GITHUB COLOR SCALE ---
          backgroundColor: (ctx: any) => {
            const value = ctx.dataset.data[ctx.dataIndex]?.v || 0;
            if (value === 0) return '#EBEDF0'; // Level 0 (None)

            const normalized = maxValue > minValue
              ? (value - minValue) / (maxValue - minValue)
              : (value > 0 ? 1 : 0);

            // GitHub 4-level green scale
            if (normalized < 0.25) return '#9BE9A8'; // Level 1
            if (normalized < 0.5) return '#40C463';  // Level 2
            if (normalized < 0.75) return '#30A14E'; // Level 3
            return '#216E39';                       // Level 4
          },
          borderWidth: 1.5, // Thinner, white borders
          borderColor: '#FFFFFF',
          width: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea || xlabels.length === 0) return 10;
            return (chartArea.right - chartArea.left) / xlabels.length - 2;
          },
          height: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea || ylabels.length === 0) return 10;
            return (chartArea.bottom - chartArea.top) / ylabels.length - 2;
          },
        } as any
      ]
    };

    this.heatmapChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          type: 'category',
          labels: xlabels,
          offset: true,
          grid: { display: false, drawBorder: false }, // No grid/border
          ticks: {
            // --- HIDE X-AXIS LABELS for YEARLY ---
            display: timeFrame !== 'YEARLY',
            color: '#9ca3af',
            autoSkip: timeFrame !== 'WEEKLY',
            maxRotation: timeFrame === 'MONTHLY' ? 45 : 0,
            minRotation: timeFrame === 'MONTHLY' ? 45 : 0,
            font: { size: 11 }
          },
        },
        y: {
          type: 'category',
          labels: ylabels,
          offset: true,
          grid: { display: false, drawBorder: false }, // No grid/border
          ticks: {
            color: '#57606a', // GitHub label color
            font: { size: 11 },
            // --- GITHUB Y-AXIS LABELS ---
            callback: (value: any, index: number, ticks: any) => {
              const label = ylabels[index];
              if (timeFrame === 'YEARLY') {
                // Show only Mon, Wed, Fri
                if (label === 'Monday' || label === 'Wednesday' || label === 'Friday') {
                  return label.substring(0, 3);
                }
                return null; // Hide other labels
              }
              return label; // Show all labels for Weekly/Monthly
            },
            padding: 5,
          },
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: false,
          callbacks: {
            title: () => '',
            label: (ctx: any) => {
              const dataPoint = ctx.raw;
              // We'd need to pass the *actual date* for a perfect tooltip
              // But for now, we use the labels
              const dateLabel = `${dataPoint.y}, ${dataPoint.x}`;
              if (dataPoint.v === 0) return `No activity on ${dateLabel}`;

              const metricLabel = this.metricType === 'postCount' ? 'posts' : 'avg reactions';
              const value = this.metricType === 'postCount' ? dataPoint.postCount : dataPoint.avgReactions.toFixed(2);
              return `${value} ${metricLabel} on ${dateLabel}`;
            }
          }
        }
      }
    } as any;
  }
}
