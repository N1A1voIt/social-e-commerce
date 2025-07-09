import { Component } from '@angular/core';
import {SidebarComponent} from "./sidebar/sidebar.component";
import {LinkComponent} from "./sidebar/link/link.component";
import {RouterOutlet} from "@angular/router";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    SidebarComponent,
    LinkComponent,
    RouterOutlet
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {

}
