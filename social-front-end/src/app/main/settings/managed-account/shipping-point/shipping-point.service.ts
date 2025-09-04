import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ShippingPoint } from './shipping-point.model';
import { javaHost } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ShippingPointService {
  private apiUrl = `${javaHost}/api/shipping-points`;

  constructor(private http: HttpClient) { }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
  }

  createShippingPoint(shippingPoint: ShippingPoint): Observable<ShippingPoint> {
    return this.http.post<ShippingPoint>(this.apiUrl, shippingPoint, { headers: this.getHeaders() });
  }

  getShippingPointsByManagedPageId(managedPageId: number): Observable<ShippingPoint[]> {
    return this.http.get<ShippingPoint[]>(`${this.apiUrl}/managed-page/${managedPageId}`, { headers: this.getHeaders() });
  }
}
