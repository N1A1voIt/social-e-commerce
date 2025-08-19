import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {Observable} from "rxjs";
import {InboxDisplay, Message, MessageBody} from "./inbox-element.type";
export interface ApiResponse {
  status: number;
  data: any;
  errors: any[];
}
@Injectable({
  providedIn: 'root'
})
export class InboxService {

  constructor(private http: HttpClient) { }

  fetchInbox(managedPageId: number):Observable<InboxDisplay> {
      const header = {
        'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
      };
      return this.http.get<InboxDisplay>(javaHost + '/api/inbox?idMp='+managedPageId,{headers:header});
  }
  fetchPages():Observable<ManagedPageCPL[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<ManagedPageCPL[]>(javaHost + '/api/auth/managed-pages-all', { headers });
  }
  fetchMessages(conversationId: number):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost + '/api/messages?idMm='+conversationId,{headers:header});
  }
  sendMessage(message: MessageBody):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.post<ApiResponse>(javaHost + '/api/messages',message,{headers:header});
  }
  fetchAnalyses(message:string) : Observable<ApiResponse> {
    const query = {
      'query': message
    };
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.post<ApiResponse>(javaHost + '/api/messages/fetch-orders',query,{headers:header});
  }
}
