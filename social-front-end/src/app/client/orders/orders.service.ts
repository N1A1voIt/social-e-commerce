import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../environments/environment';
import { ApiResponse } from '../../main/inbox/inbox.service';

export interface CustomerOrder {
  idOrderM: number;
  description: string;
  createdAt: string;
  dTotal: number;
  dCustomerName: string;
  dStatus: number;
  statusLabel: string;
  shippingAddress: string;
  customerNumber: string;
  idSeller: number;
  sellerName: string;
  items: OrderItem[];
}

export interface OrderItem {
  idOrderDetails: number;
  price: number;
  quantity: number;
  idVariant: number;
  idProduct: number;
  mediaUrl: string;
  sku: string;
  productName: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerOrdersService {
  private baseUrl = `${javaHost}/api/customer/orders`;

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }

  getOrders(): Observable<ApiResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<ApiResponse>(this.baseUrl, { headers });
  }
}

