import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ProductCpl} from "./products.types";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";
import {ApiResponse} from "../inbox/inbox.service";

@Injectable({
  providedIn: 'root'
})
export class ProductServiceService {

  constructor(private http: HttpClient) { }
  fetchProducts():Observable<ProductCpl[]> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ProductCpl[]>(javaHost + '/api/products/cpl',{headers:header});
  }

}
