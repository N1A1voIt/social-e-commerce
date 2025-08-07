import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {Observable} from "rxjs";
import {InboxDisplay, Message} from "./inbox-element.type";

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
  fetchMessages(conversationId: number):Observable<Message[]> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<Message[]>(javaHost + '/api/inbox/messages?idConversation='+conversationId,{headers:header});
  }

}
