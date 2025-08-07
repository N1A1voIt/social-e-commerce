import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {InboxService} from "./inbox.service";
import {InboxDisplay} from "./inbox-element.type";
import {ManagedAccountComponent} from "../settings/managed-account/managed-account.component";
import {PageListComponent} from "./page-list/page-list.component";

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, ManagedAccountComponent, PageListComponent],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.css'
})
export class InboxComponent implements OnInit {
  showChatOnMobile: boolean = false;
  isTyping: boolean = false;
  message: string = '';
  pages:ManagedPageCPL[] = [];
  inbox!:InboxDisplay;
  constructor(private inboxService: InboxService) { }
  ngOnInit(): void {
    setTimeout(() => {
      this.isTyping = true;
      setTimeout(() => {
        this.isTyping = false;
      }, 3000);
    }, 1000);
    this.inboxService.fetchPages().subscribe({
      next: (response) => {
        this.pages = response;
        if (this.pages.length > 0) {
          this.fetchInboxContent(this.pages[0].idMp);
        }
      },error(err) {
        alert(err.message);
      }
    });
  }
  fetchInboxContent(pageId:number) {
    this.inboxService.fetchInbox(pageId).subscribe({
      next: (response) => {
        this.inbox = response;
      },error(err) {
        alert(err.message);
      }
    });
  }
  toggleChatView(): void {
    this.showChatOnMobile = !this.showChatOnMobile;
  }

  onMessageInput(event: any): void {
    this.message = event.target.value;
  }

  sendMessage(): void {
    if (this.message.trim()) {
      // Here you would typically send the message to a service
      console.log('Sending message:', this.message);
      this.message = '';
    }
  }
}
