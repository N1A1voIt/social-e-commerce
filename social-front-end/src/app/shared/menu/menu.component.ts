import { Component } from '@angular/core';
import {SidebarComponent} from "./sidebar/sidebar.component";
import {LinkComponent} from "./sidebar/link/link.component";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    SidebarComponent,
    LinkComponent
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {

}
