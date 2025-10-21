import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix';
import { Chart } from 'chart.js';
import {HeatmapData} from "../dashboard.service";

// Register the matrix chart type
Chart.register(MatrixController, MatrixElement);


@Component({
  selector: 'app-heatmap-chart',
  standalone: true,
  imports: [NgChartsModule],
  templateUrl: 'heatmap.component.html',
  styleUrls: ['./heatmap.component.css']
})
export class HeatmapComponent implements OnInit, OnChanges {
  public heatmapChartType: 'matrix' = 'matrix';
  @Input() data!: HeatmapData;
  @Input() metricType: 'postCount' | 'avgReactions' = 'postCount'; // Choose which metric to display

  public heatmapChartData: ChartConfiguration<'matrix'>['data'] = { datasets: [] };
  public heatmapChartOptions: ChartConfiguration<'matrix'>['options'] = {};

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) {
      this.updateHeatmap();
    }
    if (changes['metricType']) {
      this.updateHeatmap();
    }
  }

  ngOnInit(): void {
    if (this.data) {
      this.updateHeatmap();
    }
  }

  private updateHeatmap() {
    if (!this.data || !this.data.cells || this.data.cells.length === 0) {
      return;
    }

    const { xlabels, ylabels, cells } = this.data;

    // Extract values based on selected metric
    const values = cells.map(cell =>
      this.metricType === 'postCount' ? cell.postCount : cell.avgReactions
    );
    const maxValue = Math.max(...values, 1); // Ensure at least 1 to avoid division by zero
    const minValue = Math.min(...values);

    // Transform cells to matrix format
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
          label: this.getChartLabel(),
          data: matrixData as any,
          backgroundColor: (ctx: any) => {
            const value = ctx.dataset.data[ctx.dataIndex]?.v || 0;
            const normalized = maxValue > minValue
              ? (value - minValue) / (maxValue - minValue)
              : 0;

            // Color scheme based on timeframe
            return this.getColorForValue(normalized);
          },
          borderWidth: 1,
          borderColor: 'rgba(255, 255, 255, 0.2)',
          width: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea) return 40;
            return (chartArea.right - chartArea.left) / xlabels.length - 2;
          },
          height: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea) return 40;
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
          grid: { display: false },
          ticks: {
            color: '#9ca3af',
            autoSkip: false,
            maxRotation: this.data.timeFrame === 'WEEKLY' ? 0 : 45,
            minRotation: this.data.timeFrame === 'WEEKLY' ? 0 : 45,
            font: {
              size: 11
            }
          },
        },
        y: {
          type: 'category',
          labels: ylabels,
          offset: true,
          grid: { display: false },
          ticks: {
            color: '#9ca3af',
            font: {
              size: 11
            }
          },
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: () => '',
            label: (ctx: any) => {
              const dataPoint = ctx.raw;
              return [
                `${dataPoint.y} - ${dataPoint.x}`,
                `Posts: ${dataPoint.postCount}`,
                `Avg Reactions: ${dataPoint.avgReactions.toFixed(2)}`
              ];
            }
          },
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          displayColors: false
        }
      }
    } as any;
  }

  private getColorForValue(normalized: number): string {
    // Use different color schemes based on timeframe or preference
    // Option 1: Gray scale (original)
    // return `rgba(107, 114, 128, ${normalized})`;

    // Option 2: Blue gradient (more engaging)
    const r = Math.round(59 + (99 - 59) * (1 - normalized));
    const g = Math.round(130 + (179 - 130) * (1 - normalized));
    const b = Math.round(246 + (255 - 246) * (1 - normalized));
    return `rgba(${r}, ${g}, ${b}, ${0.3 + normalized * 0.7})`;

    // Option 3: Heat map (red-yellow gradient)
    // const r = Math.round(255);
    // const g = Math.round(255 * (1 - normalized));
    // const b = Math.round(0);
    // return `rgba(${r}, ${g}, ${b}, ${0.3 + normalized * 0.7})`;
  }

  private getChartLabel(): string {
    const metric = this.metricType === 'postCount' ? 'Post Count' : 'Average Reactions';
    const timeFrame = this.data.timeFrame.charAt(0) + this.data.timeFrame.slice(1).toLowerCase();
    return `${timeFrame} ${metric} Heatmap`;
  }
}
