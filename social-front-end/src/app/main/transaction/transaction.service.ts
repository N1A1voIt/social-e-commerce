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
}
