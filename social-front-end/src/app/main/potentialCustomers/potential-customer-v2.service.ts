import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PotentialCustomerV2Service {

  constructor(private http: HttpClient) { }

  getAllPotentialCustomers(): Observable<any> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<any>(`${javaHost}/api/potential-customers`, { headers: header });
  }

  getCustomerById(id: string): Observable<any> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<any>(`${javaHost}/api/potential-customers/${id}`, { headers: header });
  }
}
