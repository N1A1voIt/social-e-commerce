import { Component } from '@angular/core';
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {PaginatorModule} from "primeng/paginator";
import {HttpClient} from "@angular/common/http";
import {pythonHost} from "../../../environments/environment";
import {ChartDisplayComponent} from "./chart-display/chart-display.component";
import {FormsModule} from "@angular/forms";
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
    ChartDisplayComponent,
    FormsModule
  ],
  templateUrl: './generic-chat.component.html',
  styleUrl: './generic-chat.component.css'
})
export class GenericChatComponent {
  messages:AIMessage[] = [];
  query:string = '';
  url:string = pythonHost+'/generic-chat';
  isWaitingForResponse: boolean = false;

  constructor(private http: HttpClient) {

  }
  onMessageInput(event: any): void {
    this.query = event.target.value;
  }
  onSubmit() {
    if (!this.query.trim() || this.isWaitingForResponse) {
      return;
    }
    
    this.messages.push({message:this.query,chart:null,isFromMe:true});
    const query = {
      'query': this.query
    };
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    
    this.query = '';
    this.isWaitingForResponse = true;
    
    this.http.post<AIMessage>(this.url , query , {headers:header}).subscribe({
       next: (res) => {
         this.isWaitingForResponse = false;
         this.messages.push(
           {
             message:res.message,
             chart:res.chart,
             isFromMe:false
           }
         )
       },error: (err) => {
         this.isWaitingForResponse = false;
         console.log(err)
         alert(err)
       }
     }
     )
  }
}
