import { Component } from '@angular/core';
import {SidebarComponent} from "./sidebar/sidebar.component";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    SidebarComponent
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {

}
