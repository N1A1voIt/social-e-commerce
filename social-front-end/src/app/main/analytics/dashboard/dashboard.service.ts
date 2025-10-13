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

export interface DashboardStats {
  totalRevenue: number;
  revenuePerUser: number;
  bestDeal: number;
  totalSales: number;
  dateRange: string;
  platformRepartition: PlatformRepartitionDto[];
  pagesRepartition: PagesRepartitionDto[];

}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) { }

  getDashboardStats(): Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost + '/api/dashboard/stats', { headers: header });
  }
}
