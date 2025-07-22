import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {javaHost} from "../../../../environments/environment";

export interface ManagedPage {
  idMp: number;
  pageTitle: string;
  platform: string;
}

@Injectable({ providedIn: 'root' })
export class PageService {
  constructor(private http: HttpClient) {}

  validate(platform: string, uuid: string, token: string): Observable<ManagedPage[]> {
    const headers = new HttpHeaders().set('Authorization', token);
    const params = new HttpParams().set('uuid', uuid);

    return this.http.get<ManagedPage[]>(`${javaHost}/api/auth/${platform}/managed-pages`, { headers, params });
  }
}

