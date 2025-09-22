import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ApiResponse} from "../../main/inbox/inbox.service";
import {javaHost} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class MissionHistoryService {
  constructor(private http: HttpClient) { }

  /**
   * Fetch all delivery missions for the current delivery driver
   */
  getPreviousMissions(): Observable<ApiResponse> {
    const headers = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(`${javaHost}/api/delivery/space/missions/completed`, { headers });
  }
  getPreviousRequests() : Observable<ApiResponse> {
    const headers = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(`${javaHost}/api/delivery/space/missions/pending-requests`, { headers });
  }
}
