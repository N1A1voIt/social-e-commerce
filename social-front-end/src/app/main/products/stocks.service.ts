import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ApiResponse} from "../inbox/inbox.service";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class StocksService {

  constructor(private http: HttpClient) { }
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token')?.replace('Bearer ', '');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }
  fetchStockUtilities():Observable<ApiResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<ApiResponse>(javaHost+"/api/stocks/utils?page=0&size=1000",{ headers });
  }
  saveStocks(stock:any):Observable<ApiResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<ApiResponse>(javaHost+"/api/stocks",stock,{ headers });
  }
}
