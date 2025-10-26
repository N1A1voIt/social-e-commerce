import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../../environments/environment';
import {ApiResponse} from "../../../main/inbox/inbox.service";

export interface CheckoutRequest {
  sellerId: number;
}

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {
  private baseUrl = `${javaHost}/api/customer/checkout`;

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }

  checkout(request: CheckoutRequest): Observable<ApiResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<ApiResponse>(this.baseUrl, request, { headers });
  }
}
