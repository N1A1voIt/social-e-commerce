import {Component, Input} from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-action-badge',
  standalone: true,
    imports: [
        NgIcon,
        NgIf
    ],
  templateUrl: './action-badge.component.html',
  styleUrl: './action-badge.component.css'
})
export class ActionBadgeComponent {
  @Input() href!:string;
  @Input() icon!:string;
  @Input() isActive:boolean = false;
}
