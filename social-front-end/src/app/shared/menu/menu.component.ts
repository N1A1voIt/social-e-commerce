import { Component, OnInit, OnDestroy } from '@angular/core';
import { SidebarComponent } from "./sidebar/sidebar.component";
import { LinkComponent } from "./sidebar/link/link.component";
import { TreeExampleComponent } from "./sidebar/tree-example/tree-example.component";
import { RouterOutlet } from "@angular/router";
import { NgIf, NgClass } from "@angular/common";
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AccountDetailsComponent } from "../../main/settings/account-details/account-details.component";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    SidebarComponent,
    LinkComponent,
    TreeExampleComponent,
    RouterOutlet,
    NgIf,
    NgClass,
    AccountDetailsComponent
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit, OnDestroy {
  isMobile: boolean = false;
  isSidebarOpen: boolean = false;
  isAccountDetailsVisible: boolean = false;
  private destroy$ = new Subject<void>();

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit(): void {
    this.setupScreenSizeDetection();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupScreenSizeDetection(): void {
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small])
      .pipe(takeUntil(this.destroy$))
      .subscribe(result => {
        this.isMobile = result.matches;
      });
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  toggleAccountDetails(): void {
    this.isAccountDetailsVisible = !this.isAccountDetailsVisible;
  }
}
