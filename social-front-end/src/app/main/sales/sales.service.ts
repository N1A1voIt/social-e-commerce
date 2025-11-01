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

  fetchAllSales(pageNum: number, size: number = 20, sort?: string): Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    let url = `${javaHost}/api/sales?page=${pageNum}&size=${size}`;
    if (sort) {
      url += `&sort=${encodeURIComponent(sort)}`;
    }
    return this.http.get<ApiResponse>(url, { headers: header as any });
  }
}
