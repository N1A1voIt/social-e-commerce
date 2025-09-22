import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AmountDistance } from './amount-distance.model';
import { javaHost } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AmountDistanceService {
  private apiUrl = `${javaHost}/api/amount-distances`;

  constructor(private http: HttpClient) { }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
  }

  createAmountDistance(amountDistance: AmountDistance): Observable<AmountDistance> {
    return this.http.post<AmountDistance>(this.apiUrl, amountDistance, { headers: this.getHeaders() });
  }

  getAmountDistancesByManagedPageId(managedPageId: number): Observable<AmountDistance[]> {
    return this.http.get<AmountDistance[]>(`${this.apiUrl}/managed-page/${managedPageId}`, { headers: this.getHeaders() });
  }

  updateAmountDistance(id: number, amountDistance: AmountDistance): Observable<AmountDistance> {
    return this.http.put<AmountDistance>(`${this.apiUrl}/${id}`, amountDistance, { headers: this.getHeaders() });
  }

  deleteAmountDistance(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
