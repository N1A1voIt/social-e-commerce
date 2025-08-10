import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Variant} from "../products.types";
import {javaHost} from "../../../../environments/environment";
import {Observable} from "rxjs";
import {ApiResponse} from "../../inbox/inbox.service";

@Injectable({
  providedIn: 'root'
})
export class VariantsService {

  constructor(private http:HttpClient) { }
  fetchVariants(idProduct:number):Observable<ApiResponse> {
    return this.http.get<ApiResponse>(javaHost+'/api/variants/'+idProduct);
  }
}
