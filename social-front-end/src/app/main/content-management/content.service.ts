import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {Product} from "../products/products.types";
import {ApiResponse} from "../inbox/inbox.service";
import {ManagedPage} from "../authentication/validate-pages/page.service";

export interface MotherPostDisplay {
  idPost:number,
  scheduled: boolean,
  title: string,
  creationDate : Date,
  description: string,
}

export interface Media {
  id: number,
  mediaUrl: string,
  idChild: number
}

export interface PostChild {
  id: number,
  postUrl: string,
  mediaUrl?: string,
  description?: string,
  platformIdentifier: string,
  type?: string,
  idSp: number,
  idChild1?: number,
  idPost: number,
  mediaList: Media[],
  attachments?: PostChild[]
}

export interface PostStatistics {
  platformReactions: PlatformReactionDistribution[],
  likesTimeSeries: LikesTimeSeries[],
  totalLikes: number,
  totalViews: number,
  totalComments: number,
  totalShares: number
}

export interface PlatformReactionDistribution {
  platformName: string,
  platformId: number,
  likesCount: number,
  percentage: number
}

export interface LikesTimeSeries {
  date: string,
  likesCount: number,
  platformName: string,
  platformId: number
}

export interface PostUtilities {
  managedPages: ManagedPageCPL[],
  products: Product[]
}

export interface PostsResponse {
  posts: MotherPostDisplay[],
  totalPosts: number
}

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  constructor(private http: HttpClient) { }
  
  fetchContent(page: number, size: number, filters?: any):Observable<PostsResponse> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    
    let url = `${javaHost}/api/posts/fetch-mother?page=${page}&size=${size}`;
    
    if (filters) {
      if (filters.title) {
        url += `&title=${encodeURIComponent(filters.title)}`;
      }
      if (filters.type) {
        url += `&type=${encodeURIComponent(filters.type)}`;
      }
      if (filters.startDate) {
        url += `&startDate=${filters.startDate}`;
      }
      if (filters.endDate) {
        url += `&endDate=${filters.endDate}`;
      }
    }
    
    return this.http.get<PostsResponse>(url, {headers});
  }
  public fetchUtilities(): Observable<PostUtilities> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<PostUtilities>(javaHost + '/api/posts/fetch-utilities?page=0&size=10', { headers });
  }
  fetchPageIds() : Observable<ManagedPage[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<ManagedPage[]>(javaHost + '/api/posts/fetch-page-ids', { headers });
  }

  fetchPostChildren(postId: number): Observable<PostChild[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<PostChild[]>(`${javaHost}/api/posts/${postId}/children`, { headers });
  }

  fetchPostStatistics(postId: number): Observable<PostStatistics> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    return this.http.get<PostStatistics>(`${javaHost}/api/posts/${postId}/statistics`, { headers });
  }
}
