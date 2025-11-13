import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../inbox/inbox.service';
import { javaHost } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SalesService {
  constructor(private http: HttpClient) { }

  fetchAllSales(pageNum: number, size: number = 20, sort?: string, filters?: any): Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    let url = `${javaHost}/api/sales?page=${pageNum}&size=${size}`;
    if (sort) {
      url += `&sort=${encodeURIComponent(sort)}`;
    }
    
    // Add filters to URL
    if (filters) {
      if (filters.status !== undefined && filters.status !== null) {
        url += `&status=${filters.status}`;
      }
      if (filters.fromName) {
        url += `&fromName=${encodeURIComponent(filters.fromName)}`;
      }
      if (filters.orderId) {
        url += `&orderId=${encodeURIComponent(filters.orderId)}`;
      }
      if (filters.startDate) {
        url += `&startDate=${filters.startDate}`;
      }
      if (filters.endDate) {
        url += `&endDate=${filters.endDate}`;
      }
    }
    
    return this.http.get<ApiResponse>(url, { headers: header as any });
  }
  payFull(idSales:number):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(`${javaHost}/api/sales/paid/${idSales}`, { headers: header as any });
  }

  importCsv(file: File): Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse>(`${javaHost}/api/sales/import`, formData, { headers: header as any });
  }
}
