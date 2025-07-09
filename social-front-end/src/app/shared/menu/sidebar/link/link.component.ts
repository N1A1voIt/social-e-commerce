import {Component, Input} from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-link',
  standalone: true,
  imports: [
    NgIcon,
    NgIf
  ],
  templateUrl: './link.component.html',
  styleUrl: './link.component.css'
})
export class LinkComponent {
  @Input() href!:string;
  @Input() icon!:string;
  @Input() isActive:boolean = false;
}
