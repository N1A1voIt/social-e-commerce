import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";
import {ManagedPageCPL} from "../settings/account-details/account-details.component";

export interface MotherPostDisplay {
  idPost:number,
  scheduled: boolean,
  title: string,
  creationDate : Date
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
  public fetchManagedPages(): Observable<ManagedPageCPL[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<ManagedPageCPL[]>(javaHost + '/api/auth/managed-pages-all', { headers });
  }
}
