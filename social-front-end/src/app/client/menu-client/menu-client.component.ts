import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from "@angular/router";
import { LinkComponent } from "../../shared/menu/sidebar/link/link.component";
import { NgIcon } from "@ng-icons/core";
import { NavButtonComponent } from "./nav-button/nav-button.component";
import { CommonModule } from "@angular/common";
import { filter } from "rxjs";

interface MenuItem {
  icon: string;
  label: string;
  route: string;
  isActive: boolean;
}

@Component({
  selector: 'app-menu-client',
  standalone: true,
  imports: [
    RouterOutlet,
    LinkComponent,
    NgIcon,
    NavButtonComponent,
    CommonModule
  ],
  templateUrl: './menu-client.component.html',
  styleUrls: ['./menu-client.component.css']
})
export class MenuClientComponent implements OnInit {
  menuOpen = false;

  menuItems: MenuItem[] = [
    { icon: 'heroHome', label: 'Marketplace', route: '/client/marketplace', isActive: false },
    { icon: 'heroShoppingCart', label: 'Cart', route: '/client/cart', isActive: false },
    { icon: 'heroShoppingBag', label: 'Orders', route: '/client/orders', isActive: false }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Update active state on initialization
    this.updateActiveState(this.router.url);

    // Listen to route changes
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateActiveState(event.url);
      });
  }

  updateActiveState(currentUrl: string): void {
    this.menuItems.forEach(item => {
      // Check if the current URL starts with the item route or matches it
      item.isActive = currentUrl.startsWith(item.route);
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    localStorage.removeItem('client_token');
    this.router.navigate(['/client/auth/login']);
  }

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
