import { Component } from '@angular/core';
import {NgClass, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-post-body',
  standalone: true,
  imports: [
    NgClass,
    NgForOf,
    NgIf
  ],
  templateUrl: './post-body.component.html',
  styleUrl: './post-body.component.css'
})
export class PostBodyComponent {
  images: string[] = [
    'assets/imgs/gustavo.jpeg',
    'assets/imgs/gustavo.jpeg',
    'assets/imgs/gustavo.jpeg',
    'assets/imgs/gustavo.jpeg',
    'assets/imgs/gustavo.jpeg',

    // Add more if needed
  ];

  get displayImages(): string[] {
    // Show only first 4 if more than 4
    return this.images.length > 4 ? this.images.slice(0, 4) : this.images;
  }

  get extraCount(): number {
    return this.images.length > 4 ? this.images.length - 3 : 0;
  }

  getGridColsClass(): string {
    const count = this.images.length;

    if (count === 1) return 'grid-cols-1';
    if (count === 2) return 'grid-cols-2';
    if (count === 3) return 'grid-cols-3';

    // For 4 or more, use 3 columns
    return 'grid-cols-3 grid-rows-2';
  }
}
