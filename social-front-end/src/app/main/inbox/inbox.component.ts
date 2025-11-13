import { Component, OnInit } from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ManagedPageCPL} from "../settings/account-details/account-details.component";
import {ApiResponse, InboxService} from "./inbox.service";
import {InboxDisplay, Message, MessageBody, MessageBox} from "./inbox-element.type";
import {ManagedAccountComponent} from "../settings/managed-account/managed-account.component";
import {PageListComponent} from "./page-list/page-list.component";
import {InboxPopupComponent} from "./inbox-popup/inbox-popup.component";
import {OrderPreview, VariantWithQuantity} from "../products/products.types";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule, ManagedAccountComponent, PageListComponent, InboxPopupComponent],
  providers:[DatePipe],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.css'
})
export class InboxComponent implements OnInit {
  showChatOnMobile: boolean = false;
  isTyping: boolean = false;
  message: string = '';
  pages:ManagedPageCPL[] = [];
  inbox!:InboxDisplay;
  showPageList: boolean = false;
  messages:Message[] = [];
  actualCustomer!: MessageBox;
  orderPreview!: OrderPreview;
  openPopup: boolean = false;
  loadingOrders: boolean = false;
  orderMessage: string = '';
  pageInbox!: ManagedPageCPL;
  changePage(page:ManagedPageCPL) {
    this.pageInbox = page;
    this.fetchInboxContent(page.idMp);
  }


  fetchMessages(customer:MessageBox) {
    console.log("SUTOMER"+JSON.stringify(customer) );
    this.actualCustomer = customer;
    this.actualCustomer.id_pc = customer.idPc;
    this.showChatOnMobile = !this.showChatOnMobile;
    this.inboxService.fetchMessages(customer.idMm).subscribe({
      next: (response) => {
        console.log(response);
        this.messages = response.data;
      },error(err) {
        alert(err.message);
      }
    });
  }



  getFormattedDate(dateStr: string | Date): string {
    const date = new Date(dateStr);
    const now = new Date();

    const isToday =
      date.getDate() === now.getDate() &&
      date.getMonth() === now.getMonth() &&
      date.getFullYear() === now.getFullYear();

    if (isToday) {
      // Show time only (e.g., 3:45 PM)
      return this.datePipe.transform(date, 'h:mm a')!;
    } else if (date.getFullYear() === now.getFullYear()) {
      // Show short date (e.g., Aug 7)
      return this.datePipe.transform(date, 'MMM d')!;
    } else {
      // Show full date (e.g., Aug 7, 2024)
      return this.datePipe.transform(date, 'MMM d, y')!;
    }
  }

  constructor(
    private inboxService: InboxService,
    private datePipe: DatePipe,
    private route: ActivatedRoute
  ) { }

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
          // Check if there are query params to navigate to a specific conversation
          this.route.queryParams.subscribe(params => {
            const customerId = params['customerId'];
            const pageId = params['pageId'];
            
            if (pageId && customerId) {
              // Find the page that matches the pageId
              const targetPage = this.pages.find(p => p.idMp === parseInt(pageId));
              if (targetPage) {
                this.pageInbox = targetPage;
                this.fetchInboxContent(parseInt(pageId), customerId);
              } else {
                // Default to first page if not found
                this.fetchInboxContent(this.pages[0].idMp);
              }
            } else {
              // No query params, load first page as usual
              this.fetchInboxContent(this.pages[0].idMp);
            }
          });
        }
      },
      error(err) {
        alert(err.message);
      }
    });
  }
  
  fetchInboxContent(pageId: number, autoOpenCustomerId?: string) {
    this.inboxService.fetchInbox(pageId).subscribe({
      next: (response) => {
        console.log(response);
        this.inbox = response;
        
        // If customerId is provided, automatically open that conversation
        if (autoOpenCustomerId && this.inbox.mothers) {
          const targetCustomer = this.inbox.mothers.find(
            (msg: MessageBox) => msg.idPc === autoOpenCustomerId
          );
          if (targetCustomer) {
            this.fetchMessages(targetCustomer);
          }
        }
      },
      error(err) {
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
    const message:MessageBody = {
        idMm: this.actualCustomer.idMm,
        message: this.message,
        platform: this.actualCustomer.platform
    };
    this.inboxService.sendMessage(message).subscribe({
      next: (response:ApiResponse) => {
        this.messages.push(response.data);
      }, error(err) {
        alert(err.message);
      }
    });
  }
  analyzeMessage(message:string):void {
    this.orderMessage = message;
    this.openPopup = true;
    this.loadingOrders = true;
    this.inboxService.fetchAnalyses(message).subscribe({
      next: (response:ApiResponse) => {
        console.log(response);
        this.orderPreview = response.data;
        this.orderPreview.idPc = this.actualCustomer.id_pc;
        console.log(this.orderPreview);
        this.loadingOrders = false;
      }, error(err) {
        alert(err.message);
      }
    });
  }
}
