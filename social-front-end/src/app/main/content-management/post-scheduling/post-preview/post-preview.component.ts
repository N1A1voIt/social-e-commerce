import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule, NgForOf, NgIf} from '@angular/common';
import {BeautifulButtonComponent} from "../../../../shared/beautiful-button/beautiful-button.component";
import {FormsModule} from "@angular/forms";

interface MediaDetailPreview {
  imageUrl: string;
  message: string;
}

interface PlatformPreviewItem {
  platform: string;
  mainMessage: string;
  mediaDetails: MediaDetailPreview[];
}

@Component({
  selector: 'app-post-preview',
  standalone: true,
  imports: [CommonModule, NgIf, NgForOf, BeautifulButtonComponent, FormsModule],
  templateUrl: './post-preview.component.html',
  styleUrls: ['./post-preview.component.css']
})
export class PostPreviewComponent {
  @Input() visible = false;
  @Input() data: PlatformPreviewItem[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() publish = new EventEmitter<{ scheduledAt?: string }>();

  scheduleEnabled = false;
  scheduledAt = '';

  onClose() {
    this.close.emit();
  }

  onPublish() {
    this.publish.emit({
      scheduledAt: this.scheduleEnabled ? this.scheduledAt : undefined
    });
  }

  getMinDateTime(): string {
    // Get current date/time and format for datetime-local input
    const now = new Date();
    now.setMinutes(now.getMinutes() + 5); // Add 5 minutes buffer
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}

