import {Component, Input} from '@angular/core';
import {MotherPostDisplay, PostChild, Media} from "../content.service";
import {NgClass, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-post-children',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    NgClass
  ],
  templateUrl: './post-children.component.html',
  styleUrl: './post-children.component.css'
})
export class PostChildrenComponent {
  @Input() post!: MotherPostDisplay;
  @Input() children: PostChild[] = [];

  openPostUrl(url: string) {
    window.open(url, '_blank');
  }

  isFacebookPost(child: PostChild): boolean {
    return child.idSp === 1; // Facebook platform ID is 1
  }

  isInstagramPost(child: PostChild): boolean {
    return child.idSp === 2; // Instagram platform ID is 2
  }

  getPlatformName(idSp: number): string {
    switch(idSp) {
      case 1: return 'Facebook';
      case 2: return 'Instagram';
      default: return 'Unknown';
    }
  }

  getPlatformColor(idSp: number): string {
    switch(idSp) {
      case 1: return 'bg-blue-100 text-blue-800';
      case 2: return 'bg-pink-100 text-pink-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }
}
