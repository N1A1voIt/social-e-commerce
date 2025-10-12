import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../inbox/inbox.service';
import { javaHost } from '../../../../environments/environment';

export interface DashboardStats {
  totalRevenue: number;
  revenuePerUser: number;
  bestDeal: number;
  totalSales: number;
  dateRange: string;
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
