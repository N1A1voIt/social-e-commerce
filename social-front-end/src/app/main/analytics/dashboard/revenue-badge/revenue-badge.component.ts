import {Component, Input} from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-revenue-badge',
  standalone: true,
  imports: [
    NgIcon,
    NgIf
  ],
  templateUrl: './revenue-badge.component.html',
  styleUrl: './revenue-badge.component.css'
})
export class RevenueBadgeComponent {
  @Input() showIcon:boolean = false;
  @Input() text:string = "";
}
