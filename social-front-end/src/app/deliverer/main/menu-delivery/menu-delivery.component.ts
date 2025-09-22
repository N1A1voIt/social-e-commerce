import { Component } from '@angular/core';
import {NavButtonComponent} from "../../../client/menu-client/nav-button/nav-button.component";
import {NgIcon} from "@ng-icons/core";
import {RouterOutlet} from "@angular/router";

@Component({
  selector: 'app-menu-delivery',
  standalone: true,
    imports: [
        NavButtonComponent,
        NgIcon,
        RouterOutlet
    ],
  templateUrl: './menu-delivery.component.html',
  styleUrl: './menu-delivery.component.css'
})
export class MenuDeliveryComponent {

}
