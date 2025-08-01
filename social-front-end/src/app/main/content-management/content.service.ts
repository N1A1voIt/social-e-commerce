import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {Product} from "../products/products.types";

export interface MotherPostDisplay {
  idPost:number,
  scheduled: boolean,
  title: string,
  creationDate : Date
}

export interface PostUtilities {
  managedPages: ManagedPageCPL[],
  products: Product[]
}

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  constructor(private http: HttpClient) { }
  fetchContent():Observable<MotherPostDisplay[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<MotherPostDisplay[]>(`${javaHost}/api/posts/fetch-mother`, {headers});
  }
  public fetchUtilities(): Observable<PostUtilities> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<PostUtilities>(javaHost + '/api/posts/fetch-utilities?page=0&size=10', { headers });
  }
}
