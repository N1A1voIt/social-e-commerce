import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ContentService, MotherPostDisplay, PostChild, PostStatistics} from "../content.service";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
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
    const platformGroups = this.groupTimeSeriesByPlatform(statistics.likesTimeSeries);
    // const datasets = Object.keys(platformGroups).map((platform, index) => ({
    //   label: platform,
    //   data: platformGroups[platform].map(item => item.likesCount),
    //   borderColor: platform === 'Facebook' ? '#3B82F6' : platform === 'Instagram' ? '#EC4899' : '#6B7280',
    //   backgroundColor: platform === 'Facebook' ? '#3B82F680' : platform === 'Instagram' ? '#EC489980' : '#6B728080',
    //   tension: 0.4,
    //   fill: false
    // }));
    //
    // this.lineChartData = {
    //   labels: this.getUniqueDates(statistics.likesTimeSeries),
    //   datasets: datasets
    // };
  }

  groupTimeSeriesByPlatform(timeSeries: any[]) {
    return timeSeries.reduce((groups, item) => {
      const platform = item.platformName;
      if (!groups[platform]) {
        groups[platform] = [];
      }
      groups[platform].push(item);
      return groups;
    }, {});
  }

  getUniqueDates(timeSeries: any[]) {
    const dates = [...new Set(timeSeries.map(item => item.date))];
    return dates.sort();
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
        likes: this.statistics.totalLikes,
        comments: this.statistics.totalComments,
        shares: this.statistics.totalShares
      };
    }
    return {
      views: 0,
      likes: 0,
      comments: 0,
      shares: 0
    };
  }
}
