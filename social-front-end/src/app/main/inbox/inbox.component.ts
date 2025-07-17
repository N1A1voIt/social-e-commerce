import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.css'
})
export class InboxComponent implements OnInit {
  showChatOnMobile: boolean = false;
  isTyping: boolean = false;
  message: string = '';

  ngOnInit(): void {
    // Simulate typing indicator after a delay
    setTimeout(() => {
      this.isTyping = true;

      // Hide typing indicator after 3 seconds
      setTimeout(() => {
        this.isTyping = false;
      }, 3000);
    }, 1000);
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
