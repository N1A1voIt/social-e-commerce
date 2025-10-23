import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../inbox/inbox.service';
import { javaHost } from '../../../../environments/environment';

export interface PlatformRepartitionDto {
  dummyId: number;           // dummy_id
  totalPercentage: number;   // total_percentage
  total: number;
  idSp: number;              // id_sp
}

export interface PagesRepartitionDto {
  dummyId: number;           // dummy_id
  totalPercentage: number;   // total_percentage
  total: number;
  pageTitle: string;         // page_title
  idSp: number;              // id_sp
  idManagedPages: number;    // id_managed_pages
  associatedMedia: string;
}
export interface SalesProgressionDto {
  labels: string[];
  data: number[];
}
export interface DashboardStats {
  totalRevenue: number;
  revenuePerUser: number;
  bestDeal: number;
  totalSales: number;
  dateRange: string;
  platformRepartition: PlatformRepartitionDto[];
  pagesRepartition: PagesRepartitionDto[];
  salesProgressionDto: SalesProgressionDto;
  bestTimeToPost: BestTimeToPost;
  heatmapData: HeatmapData;
}
export interface HeatmapCell {
  x: string;
  y: string;
  postCount: number;
  avgReactions: number;
}

export interface HeatmapData {
  xlabels: string[];
  ylabels: string[];
  cells: HeatmapCell[];
  timeFrame: 'WEEKLY' | 'MONTHLY' | 'YEARLY';
}
export interface BestTimeToPost {
  day:string;
  hour:number;
}

export interface DashboardRequest {
  startDate: Date;
  endDate: Date;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) { }

  getDashboardStats(dashboardRequest: DashboardRequest): Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.post<ApiResponse>(javaHost + '/api/dashboard/stats', dashboardRequest , { headers: header });
  }
}
