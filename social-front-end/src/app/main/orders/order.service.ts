import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ApiResponse} from "../inbox/inbox.service";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private http:HttpClient) { }

  fetchAllOrders():Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost+'/orders',{headers:header});
  }

  fetchOrderChild(id:number):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost+'/orders/'+id,{headers:header});
  }
}
