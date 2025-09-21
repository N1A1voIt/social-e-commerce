import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Delivery, DeliveryResponse } from '../models/delivery.model';
import { javaHost } from '../../../environments/environment';
import {ApiResponse} from "../../main/inbox/inbox.service";

@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  constructor(private http: HttpClient) { }

  /**
   * Fetch all delivery missions for the current delivery driver
   */
  getDeliveryMissions(): Observable<ApiResponse> {
    const headers = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(`${javaHost}/api/delivery/space/missions`, { headers });
  }

  /**
   * Update the status of a delivery mission
   */
  updateDeliveryStatus(deliveryId: number, status: string): Observable<DeliveryResponse> {
    const headers = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.put<DeliveryResponse>(
      `${javaHost}/api/deliveries/${deliveryId}/status`,
      { status },
      { headers }
    );
  }
}
