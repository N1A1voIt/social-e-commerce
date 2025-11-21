import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, Chart } from 'chart.js';
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix';
import { HeatmapData } from "../dashboard.service";
import { CommonModule } from "@angular/common";

// Register the Matrix chart components
Chart.register(MatrixController, MatrixElement);

@Component({
  selector: 'app-heatmap-chart',
  standalone: true,
  imports: [NgChartsModule, CommonModule],
  templateUrl: 'heatmap.component.html',
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
  `]
})
export class HeatmapComponent implements OnChanges {
  public heatmapChartType: 'matrix' = 'matrix';

  @Input() data!: HeatmapData;

  public heatmapChartData: ChartConfiguration<'matrix'>['data'] = { datasets: [] };
  public heatmapChartOptions: ChartConfiguration<'matrix'>['options'] = {};

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] && this.data) {
      this.updateHeatmap();
    }
  }

  getTotalLikes(): number {
    if (!this.data || !this.data.cells) return 0;
    return this.data.cells.reduce((sum, cell) => sum + (cell.likeCount || 0), 0);
  }

  // Helper method to get ordinal suffix (1st, 2nd, 3rd, etc.)
  private getOrdinal(day: number): string {
    const suffix = ["th", "st", "nd", "rd"];
    const value = day % 100;
    const ordinalSuffix = suffix[(value - 20) % 10] || suffix[value] || suffix[0];
    return day + ordinalSuffix;
  }

  // Helper method to convert week/day coordinates to formatted date string
  private getDateString(weekIndex: number, dayIndex: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    
    // Assuming the year starts from current year and week 0 is the first week of January
    const currentYear = new Date().getFullYear();
    const firstDayOfYear = new Date(currentYear, 0, 1);
    
    // Calculate the actual date based on week and day indices
    // Week 0 = first week, dayIndex 0 = Sunday, 1 = Monday, etc.
    const daysSinceStart = (weekIndex * 7) + dayIndex;
    const targetDate = new Date(firstDayOfYear.getTime() + (daysSinceStart * 24 * 60 * 60 * 1000));
    
    const day = targetDate.getDate();
    const month = months[targetDate.getMonth()];
    
    return `${this.getOrdinal(day)} ${month}`;
  }

  // Calculates the minimum width required to keep cells square (~15px each)

  getChartWidth(): string {
    const weeks = this.data?.xlabels?.length || 53;
    // 15px per week + 40px padding for Y-axis labels
    return `${(weeks * 15) + 40}px`;
  }

  private updateHeatmap() {
    console.log('updateHeatmap called with data:', this.data);
    
    if (!this.data || !this.data.cells || this.data.cells.length === 0) {
      console.log('No data or empty cells, resetting chart');
      this.heatmapChartData = { datasets: [] };
      return;
    }

    const { xlabels, ylabels, cells } = this.data;
    console.log('Processing heatmap data:', { xlabels, ylabels, cellsCount: cells.length });

    // 1. Calculate Data Range for Colors
    const values = cells.map(c => c.likeCount || 0);
    const max = Math.max(...values.map(v => Math.abs(v)), 1); // Use absolute values for intensity calculation
    console.log('Value range:', { values: values.slice(0, 5), max });

    // 2. Prepare Matrix Data with proper x,y mapping
    const matrixData = cells.map(cell => {
      // Handle both string and numeric x,y values
      let xVal, yVal;
      
      if (typeof cell.x === 'string') {
        // For yearly view: x = "W0", "W1", etc. -> extract number
        xVal = cell.x.charAt(0) === 'W' ? parseInt(cell.x.substring(1)) : parseInt(cell.x);
      } else {
        xVal = cell.x;
      }
      
      if (typeof cell.y === 'string') {
        // Map day names to indices
        const dayMap: { [key: string]: number } = {
          'Monday': 0, 'Tuesday': 1, 'Wednesday': 2, 'Thursday': 3,
          'Friday': 4, 'Saturday': 5, 'Sunday': 6
        };
        yVal = dayMap[cell.y] !== undefined ? dayMap[cell.y] : parseInt(cell.y);
      } else {
        yVal = cell.y;
      }
      
      return {
        x: xVal,
        y: yVal,
        v: cell.likeCount || 0
      };
    });
    
    console.log('Matrix data sample:', matrixData.slice(0, 5));


    // 3. Generate X-Axis Categories
    const xAxisLabels = xlabels || [];
    const yAxisLabels = ylabels || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    
    console.log('Chart labels:', { xAxisLabels, yAxisLabels });

    this.heatmapChartData = {
      datasets: [{
        label: 'Contributions',
        data: matrixData,
        backgroundColor: (ctx: any) => {
          const value = ctx.raw?.v || 0;
          if (value === 0) return '#EBEDF0'; // Grey (No activity)

          // Red scale for negative values
          if (value < 0) {
            const intensity = Math.abs(value) / max;
            if (intensity < 0.25) return '#FFD6CC'; // Light red
            if (intensity < 0.50) return '#FF9980'; // Medium light red
            if (intensity < 0.75) return '#FF6B47'; // Medium red
            return '#FF0000'; // Deep red
          }

          // Green scale for positive values
          const intensity = value / max;
          if (intensity < 0.25) return '#9BE9A8';
          if (intensity < 0.50) return '#40C463';
          if (intensity < 0.75) return '#30A14E';
          return '#216E39';
        },
        borderColor: '#ffffff',
        borderWidth: 1,
        width: 12,
        height: 12
      }]
    };

    this.heatmapChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          enabled: true,
          displayColors: false,
          callbacks: {
            title: () => '',
            label: (context: any) => {
              const dataPoint = context.raw;
              const value = dataPoint.v || 0;
              const yLabel = yAxisLabels[dataPoint.y] || `Day ${dataPoint.y}`;
              
              // Format the reaction count
              const reactionText = Math.abs(value) === 1 ? 'reaction' : 'reactions';
              const valueText = value < 0 ? `${value}` : `${value}`;
              
              return `${valueText} ${reactionText} on ${this.data.timeFrame === 'YEARLY' && typeof dataPoint.x === 'number' ? this.getDateString(dataPoint.x, dataPoint.y) : yLabel}`;
            }
          }
        }
      },
      scales: {
        x: {
          type: 'linear',
          position: 'bottom',
          min: 0,
          max: Math.max(xAxisLabels.length - 1, 52),
          grid: { display: false },
          ticks: {
            stepSize: 1,
            callback: (value: any) => {
              const index = Math.floor(value);
              return xAxisLabels[index] || '';
            },
            font: { size: 10 },
            color: '#9CA3AF'
          }
        },
        y: {
          type: 'linear',
          min: 0,
          max: 6,
          grid: { display: false },
          ticks: {
            stepSize: 1,
            callback: (value: any) => {
              const index = Math.floor(value);
              const day = yAxisLabels[index];
              return day ? day.substring(0, 3) : '';
            },
            font: { size: 10 },
            color: '#6B7280'
          }
        }
      }
    };
    
    console.log('Chart configured successfully');
  }
}
