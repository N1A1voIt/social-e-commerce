import { Component, Input, OnChanges, OnInit, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix';
import { Chart } from 'chart.js';
import {HeatmapData} from "../dashboard.service";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

Chart.register(MatrixController, MatrixElement);

@Component({
  selector: 'app-heatmap-chart',
  standalone: true,
  imports: [NgChartsModule, CommonModule, FormsModule],
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
  @Input() metricType: 'likeCount' | 'avgReactions' = 'likeCount';
  @Output() yearChange = new EventEmitter<number>();

  public heatmapChartData: ChartConfiguration<'matrix'>['data'] = { datasets: [] };
  public heatmapChartOptions: ChartConfiguration<'matrix'>['options'] = {};

  // Year selection
  selectedYear: number = new Date().getFullYear();
  availableYears: number[] = [];

  ngOnInit(): void {
    this.initializeYears();
    if (this.data) this.updateHeatmap();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) this.updateHeatmap();
    if (changes['metricType']) this.updateHeatmap();
  }

  private initializeYears() {
    const currentYear = new Date().getFullYear();
    const startYear = 2020; // You can adjust this to your data start year
    this.availableYears = [];
    for (let year = currentYear; year >= startYear; year--) {
      this.availableYears.push(year);
    }
    this.selectedYear = currentYear;
  }

  onYearChange() {
    // Emit the selected year to parent component
    this.yearChange.emit(this.selectedYear);
  }

  previousYear() {
    if (this.canGoPreviousYear()) {
      this.selectedYear--;
      this.onYearChange();
    }
  }

  nextYear() {
    if (this.canGoNextYear()) {
      this.selectedYear++;
      this.onYearChange();
    }
  }

  canGoPreviousYear(): boolean {
    return this.selectedYear > Math.min(...this.availableYears);
  }

  canGoNextYear(): boolean {
    return this.selectedYear < Math.max(...this.availableYears);
  }

  getTotalLikes(): number {
    if (!this.data || !this.data.cells) return 0;
    return this.data.cells.reduce((sum, cell) => sum + cell.likeCount, 0);
  }

  getMinChartWidth(): string {
    if (!this.data || this.data.timeFrame !== 'YEARLY') {
      return '100%';
    }
    // For yearly view: calculate width based on number of weeks
    // Each week column is ~15px (12px square + 3px spacing)
    const weeks = this.data.xlabels?.length || 52;
    const minWidth = weeks * 15 + 80; // Add 80px for y-axis labels
    return `${minWidth}px`;
  }

  private updateHeatmap() {
    if (!this.data || !this.data.cells || this.data.cells.length === 0) {
      this.heatmapChartData = { datasets: [] };
      return;
    }

    const { xlabels, ylabels, cells, timeFrame } = this.data;

    // Validate data
    if (!xlabels || !ylabels || xlabels.length === 0 || ylabels.length === 0) {
      console.error('Invalid heatmap data: missing labels');
      this.heatmapChartData = { datasets: [] };
      return;
    }

    // Debug: Log the data structure
    console.log('Heatmap data:', { xlabels, ylabels, cellCount: cells.length, timeFrame });
    console.log('Sample cells:', cells.slice(0, 10));
    console.log('Unique Y values:', [...new Set(cells.map(c => c.y))]);

    // --- Min/Max calculation remains the same ---
    const values = cells.map(cell =>
      this.metricType === 'likeCount' ? cell.likeCount : cell.avgReactions
    );
    const nonZeroValues = values.filter(v => v > 0);
    const maxValue = nonZeroValues.length > 0 ? Math.max(...nonZeroValues) : 1;
    const minValue = nonZeroValues.length > 0 ? Math.min(...nonZeroValues) : 0;
    // --- End Min/Max ---

    const matrixData = cells.map(cell => ({
      x: cell.x,
      y: cell.y,
      v: this.metricType === 'likeCount' ? cell.likeCount : cell.avgReactions,
      likeCount: cell.likeCount,
      avgReactions: cell.avgReactions
    }));

    // For YEARLY view, we need to extract unique x values (W0, W1, W2...)
    // and map them to display labels (Jan, Feb, Mar...)
    let displayXLabels = xlabels;
    if (timeFrame === 'YEARLY') {
      // Extract unique week identifiers from cells
      const uniqueWeeks = Array.from(new Set(cells.map(c => c.x))).sort((a, b) => {
        const numA = parseInt(a.substring(1));
        const numB = parseInt(b.substring(1));
        return numA - numB;
      });
      displayXLabels = uniqueWeeks;
    }

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
          borderWidth: timeFrame === 'YEARLY' ? 2 : 1.5,
          borderColor: '#FFFFFF',
          width: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea || displayXLabels.length === 0) return 12;
            const availableWidth = chartArea.right - chartArea.left;
            const cellWidth = availableWidth / displayXLabels.length;

            // For yearly view, aim for GitHub-like size (11-14px)
            if (timeFrame === 'YEARLY') {
              return Math.max(10, Math.min(cellWidth - 3, 18));
            }
            return cellWidth - 2;
          },
          height: ({ chart }: any) => {
            const chartArea = chart.chartArea;
            if (!chartArea || ylabels.length === 0) return 12;
            const availableHeight = chartArea.bottom - chartArea.top;
            const cellHeight = availableHeight / ylabels.length;

            // For yearly view, aim for GitHub-like size (11-14px)
            if (timeFrame === 'YEARLY') {
              return Math.max(10, Math.min(cellHeight - 3, 14));
            }
            return cellHeight - 2;
          },
        } as any
      ]
    };

    this.heatmapChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      layout: {
        padding: {
          left: 5,
          right: 5,
          top: 5,
          bottom: 5
        }
      },
      scales: {
        x: {
          type: 'category',
          labels: displayXLabels,
          offset: true,
          grid: { display: false, drawBorder: false },
          ticks: {
            display: true,
            color: '#57606a',
            autoSkip: false,
            maxRotation: 0,
            minRotation: 0,
            font: { size: 10 },
            callback: (value: any, index: number, ticks: any) => {
              if (timeFrame === 'YEARLY') {
                // xlabels contains month names aligned with week positions
                // Show month name only where it exists (empty string otherwise)
                const monthLabel = xlabels[index] || '';
                return monthLabel;
              }
              return displayXLabels[index];
            },
          },
        },
        y: {
          type: 'category',
          labels: ylabels,
          offset: true,
          grid: { display: false, drawBorder: false },
          ticks: {
            color: '#57606a',
            font: { size: 10 },
            callback: (value: any, index: number, ticks: any) => {
              const label = ylabels[index];
              if (timeFrame === 'YEARLY') {
                // Show abbreviated day names (Mon, Tue, Wed, etc.)
                return label ? label.substring(0, 3) : '';
              }
              return label;
            },
            padding: 4,
          },
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: false,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 10,
          cornerRadius: 6,
          callbacks: {
            title: () => '',
            label: (ctx: any) => {
              const dataPoint = ctx.raw;
              const value = this.metricType === 'likeCount' ? dataPoint.likeCount : dataPoint.avgReactions;

              if (timeFrame === 'YEARLY') {
                // For yearly view, show more GitHub-like tooltip
                if (value === 0) {
                  return 'No likes';
                }
                const likeText = value === 1 ? 'like' : 'likes';
                return `${value} ${likeText}`;
              }

              // For other views
              const dateLabel = `${dataPoint.y}, ${dataPoint.x}`;
              if (value === 0) return `No activity on ${dateLabel}`;

              const metricLabel = this.metricType === 'likeCount' ? 'likes' : 'avg reactions';
              const displayValue = this.metricType === 'likeCount' ? value : value.toFixed(2);
              return `${displayValue} ${metricLabel} on ${dateLabel}`;
            }
          }
        }
      }
    } as any;
  }
}
