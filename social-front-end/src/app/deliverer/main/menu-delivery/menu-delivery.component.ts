import { Component, OnInit } from '@angular/core';
import {NavButtonComponent} from "../../../client/menu-client/nav-button/nav-button.component";
import {NgIcon} from "@ng-icons/core";
import {Router, RouterOutlet, RouterLink, NavigationEnd} from "@angular/router";
import {CommonModule} from "@angular/common";
import {filter} from "rxjs";

interface MenuItem {
  icon: string;
  label: string;
  route: string;
  isActive: boolean;
}

@Component({
  selector: 'app-menu-delivery',
  standalone: true,
    imports: [
        NavButtonComponent,
        NgIcon,
        RouterOutlet,
        RouterLink,
        CommonModule
    ],
  templateUrl: './menu-delivery.component.html',
  styleUrl: './menu-delivery.component.css'
})
export class MenuDeliveryComponent implements OnInit {
  menuItems: MenuItem[] = [
    { icon: 'heroHome', label: 'Dashboard', route: '/delivery/space/dashboard', isActive: false },
    { icon: 'heroClipboardDocumentList', label: 'Pending Applications', route: '/delivery/space/pending-applications', isActive: false },
    { icon: 'heroClock', label: 'Mission History', route: '/delivery/space/mission-history', isActive: false },
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
      item.isActive = currentUrl.includes(item.route);
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    localStorage.removeItem('delivery_token');
    this.router.navigate(['/delivery/signin']);
  }
}
