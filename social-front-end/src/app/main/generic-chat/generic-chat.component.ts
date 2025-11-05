import { Component } from '@angular/core';
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {PaginatorModule} from "primeng/paginator";
import {HttpClient} from "@angular/common/http";
import {pythonHost} from "../../../environments/environment";
import {ChartDisplayComponent} from "./chart-display/chart-display.component";
export interface AIMessage {
  message: string;
  chart: any;
  isFromMe: boolean;
}
@Component({
  selector: 'app-generic-chat',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    PaginatorModule,
    NgClass,
    ChartDisplayComponent
  ],
  templateUrl: './generic-chat.component.html',
  styleUrl: './generic-chat.component.css'
})
export class GenericChatComponent {
  messages:AIMessage[] = [];
  query:string = '';
  url:string = pythonHost+'/generic-chat';

  constructor(private http: HttpClient) {

  }
  onMessageInput(event: any): void {
    this.query = event.target.value;
  }
  onSubmit() {
    this.messages.push({message:this.query,chart:null,isFromMe:true});
    const query = {
      'query': this.query
    };
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
     this.http.post<AIMessage>(this.url , query , {headers:header}).subscribe({
       next: (res) => {
         this.messages.push(
           {
             message:res.message,
             chart:res.chart,
             isFromMe:false
           }
         )
       },error: (err) => {
         console.log(err)
         alert(err)
       }
     }
     )
  }
}
