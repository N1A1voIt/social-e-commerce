import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ApiResponse} from "../inbox.service";
import {javaHost} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class IPopupService {

  constructor(private http: HttpClient ) { }
  fetchVariantBySKU(sku:string):Observable<ApiResponse> {
    return this.http.get<ApiResponse>(javaHost + '/api/products/variant/'+sku,{headers:{'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`}});
  }
}
