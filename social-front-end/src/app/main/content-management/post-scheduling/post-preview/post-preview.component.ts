import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule, NgForOf, NgIf} from '@angular/common';

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
  imports: [CommonModule, NgIf, NgForOf],
  templateUrl: './post-preview.component.html',
  styleUrls: ['./post-preview.component.css']
})
export class PostPreviewComponent {
  @Input() visible = false;
  @Input() data: PlatformPreviewItem[] = [];
  @Output() close = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }
}

