import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ContentService, LikesTimeSeries, MotherPostDisplay, PostChild, PostStatistics} from "../content.service";
import {CommonModule, DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {ChartConfiguration, ChartData, ChartType} from 'chart.js';
import {BaseChartDirective, NgChartsModule} from 'ng2-charts';

@Component({
  selector: 'app-post-details',
  standalone: true,
  providers:[
    BaseChartDirective
  ],
  imports: [
    NgIf,
    NgForOf,
    NgClass,
    DatePipe,
    NgChartsModule,
    CommonModule,
  ],
  templateUrl: './post-details.component.html',
  styleUrl: './post-details.component.css'
})
export class PostDetailsComponent implements OnInit {
  post: MotherPostDisplay | null = null;
  children: PostChild[] = [];
  statistics: PostStatistics | null = null;
  loading = true;
  loadingStats = false;
  error: string | null = null;

  // Chart configurations
  pieChartType: ChartType = 'doughnut';
  lineChartType: ChartType = 'line';

  pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'bottom',
      },
      title: {
        display: true,
        text: 'Reactions by Platform'
      }
    }
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Likes Over Time'
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  pieChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: []
  };

  lineChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: []
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contentService: ContentService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const postId = +params['id'];
      if (postId) {
        this.loadPostDetails(postId);
      } else {
        this.error = 'Invalid post ID';
        this.loading = false;
      }
    });
  }

  loadPostDetails(postId: number) {
    this.loading = true;
    this.error = null;

    // First, get the post details from the content list
    this.contentService.fetchContent().subscribe({
      next: (posts) => {
        this.post = posts.find(p => p.idPost === postId) || null;
        if (this.post) {
          // Then fetch the post children and statistics
          this.loadPostChildren(postId);
          this.loadStatistics(postId);
        } else {
          this.error = 'Post not found';
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Error fetching posts:', err);
        this.error = 'Error loading post details';
        this.loading = false;
      }
    });
  }

  loadPostChildren(postId: number) {
    this.contentService.fetchPostChildren(postId).subscribe({
      next: (children) => {
        this.children = children;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching post children:', err);
        this.error = 'Error loading post children';
        this.loading = false;
      }
    });
  }

  loadStatistics(postId: number) {
    this.loadingStats = true;
    this.contentService.fetchPostStatistics(postId).subscribe({
      next: (statistics) => {
        this.statistics = statistics;
        this.loadingStats = false;
        this.updateCharts(statistics);
      },
      error: (err) => {
        console.error('Error fetching statistics:', err);
        this.loadingStats = false;
      }
    });
  }

  updateCharts(statistics: PostStatistics) {
    // Update pie chart data for platform distribution
    console.log(statistics)
    this.pieChartData = {
      labels: statistics.platformReactions.map(r => r.platformName),
      datasets: [{
        data: statistics.platformReactions.map(r => r.likesCount),
        backgroundColor: [
          '#3B82F6', // Blue for Facebook
          '#EC4899', // Pink for Instagram
          '#6B7280'  // Gray for others
        ],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    };

    // Update line chart data for time series
    if (statistics.likesTimeSeries && statistics.likesTimeSeries.length > 0) {
      const platformGroups = this.groupTimeSeriesByPlatform(statistics.likesTimeSeries);
      const uniqueDates = this.getUniqueDates(statistics.likesTimeSeries);
      
      const datasets = Object.keys(platformGroups).map((platform, index) => {
        const platformData = platformGroups[platform];
        // Create data array matching all dates, filling missing dates with 0
        const data = uniqueDates.map(date => {
          const item = platformData.find(p => p.date === date);
          return item ? item.likesCount : 0;
        });
        
        const colors: {[key: string]: {border: string, background: string}} = {
          'Facebook': { border: '#3B82F6', background: '#3B82F680' },
          'Instagram': { border: '#EC4899', background: '#EC489980' },
          'default': { border: '#6B7280', background: '#6B728080' }
        };
        
        const color = colors[platform] || colors['default'];
        
        return {
          label: platform,
          data: data,
          borderColor: color.border,
          backgroundColor: color.background,
          tension: 0.4,
          fill: true,
          pointRadius: 4,
          pointHoverRadius: 6,
          borderWidth: 2
        };
      });

      this.lineChartData = {
        labels: uniqueDates,
        datasets: datasets
      };
    } else {
      // No time series data available
      this.lineChartData = {
        labels: [],
        datasets: []
      };
    }
  }

  groupTimeSeriesByPlatform(timeSeries: LikesTimeSeries[]): {[platform: string]: LikesTimeSeries[]} {
    const grouped: {[platform: string]: LikesTimeSeries[]} = {};
    
    timeSeries.forEach(item => {
      if (!grouped[item.platformName]) {
        grouped[item.platformName] = [];
      }
      grouped[item.platformName].push(item);
    });
    
    // Sort each platform's data by date
    Object.keys(grouped).forEach(platform => {
      grouped[platform].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    });
    
    return grouped;
  }

  getUniqueDates(timeSeries: LikesTimeSeries[]): string[] {
    const dates = timeSeries.map(item => item.date);
    const uniqueDates = Array.from(new Set(dates));
    return uniqueDates.sort((a, b) => new Date(a).getTime() - new Date(b).getTime());
  }

  goBack() {
    this.router.navigate(['/main/content-management']);
  }

  openPostUrl(url: string) {
    window.open(url, '_blank');
  }

  isFacebookPost(child: PostChild): boolean {
    return child.idSp === 1; // Facebook platform ID is 1
  }

  isInstagramPost(child: PostChild): boolean {
    return child.idSp === 2; // Instagram platform ID is 2
  }

  getPlatformName(idSp: number): string {
    switch(idSp) {
      case 1: return 'Facebook';
      case 2: return 'Instagram';
      default: return 'Unknown';
    }
  }

  getPlatformColor(idSp: number): string {
    switch(idSp) {
      case 1: return 'bg-blue-100 text-blue-800';
      case 2: return 'bg-pink-100 text-pink-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  // Get statistics for display
  getPostStats() {
    if (this.statistics) {
      return {
        views: this.statistics.totalViews,
        likes: this.statistics.totalLikes
      };
    }
    return {
      views: 0,
      likes: 0
    };
  }
}
