import { Component } from '@angular/core';
import {RouterOutlet} from "@angular/router";
import {LinkComponent} from "../../shared/menu/sidebar/link/link.component";
import {NgIcon} from "@ng-icons/core";

@Component({
  selector: 'app-menu-client',
  standalone: true,
  imports: [
    RouterOutlet,
    LinkComponent,
    NgIcon
  ],
  templateUrl: './menu-client.component.html',
  styleUrls: ['./menu-client.component.css']
})
export class MenuClientComponent {
  menuOpen = false;

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
    const menuList = document.querySelector('.menu-list');
    if (menuList) {
      if (this.menuOpen) {
        menuList.classList.add('open');
      } else {
        menuList.classList.remove('open');
      }
    }
  }
}
