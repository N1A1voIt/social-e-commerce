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

  createShippingPoint(shippingPoint: ShippingPoint): Observable<ShippingPoint> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');

    return this.http.post<ShippingPoint>(this.apiUrl, shippingPoint, { headers });
  }
}
