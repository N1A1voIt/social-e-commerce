import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {TransactionDetail, TransactionStatus} from "./transaction.type";
import {ApiResponse} from "../inbox/inbox.service";
import {javaHost} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  constructor(private http:HttpClient) { }
  mobilePay(paymentBody:TransactionDetail):Observable<ApiResponse> {
    console.log(paymentBody)
    return this.http.post<ApiResponse>(`${javaHost}/api/order/pay?link_identifier=${paymentBody.idPayment}`,paymentBody);
  }
  fullPayment(paymentBody:TransactionDetail):Observable<ApiResponse> {
    console.log(paymentBody)
    return this.http.post<ApiResponse>(`${javaHost}/api/order/pay-full-amount?link_identifier=${paymentBody.idPayment}`,paymentBody);
  }
  
  getTempLinkDetails(linkId: string): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(`${javaHost}/api/temp-link/${linkId}`);
  }
}
