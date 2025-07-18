import {Component, Input} from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-nav-button',
  standalone: true,
    imports: [
        NgIcon,
        NgIf
    ],
  templateUrl: './nav-button.component.html',
  styleUrl: './nav-button.component.css'
})
export class NavButtonComponent {
  @Input() href!:string;
  @Input() icon!:string;
  @Input() isActive:boolean = false;
}
