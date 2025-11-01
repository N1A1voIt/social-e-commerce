import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PhoneNumber, PhoneNumberPayload } from './phone-numbers.model';
import { javaHost } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PhoneNumbersService {
  private apiUrl = `${javaHost}/api/vmpnumbers`;

  constructor(private http: HttpClient) { }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
  }

  getPhoneNumbersByManagedPageId(managedPageId: number): Observable<PhoneNumber[]> {
    return this.http.get<any>(`${this.apiUrl}/fetch-numbers/${managedPageId}`, { headers: this.getHeaders() })
      .pipe(
        map(response => {
          // Handle both response formats: { status: 200, data: [] } or direct array
          if (response && response.status === 200 && Array.isArray(response.data)) {
            return response.data;
          } else if (Array.isArray(response)) {
            return response;
          }
          return [];
        })
      );
  }

  createPhoneNumber(phoneNumber: PhoneNumberPayload): Observable<PhoneNumber> {
    return this.http.post<PhoneNumber>(this.apiUrl, phoneNumber, { headers: this.getHeaders() });
  }

  updatePhoneNumber(id: number, phoneNumber: PhoneNumberPayload): Observable<PhoneNumber> {
    return this.http.put<PhoneNumber>(`${this.apiUrl}/${id}`, phoneNumber, { headers: this.getHeaders() });
  }

  deletePhoneNumber(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}

