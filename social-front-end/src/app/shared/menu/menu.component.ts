import { Component, OnInit, OnDestroy } from '@angular/core';
import { SidebarComponent } from "./sidebar/sidebar.component";
import { LinkComponent } from "./sidebar/link/link.component";
import { RouterOutlet } from "@angular/router";
import { NgIf } from "@angular/common";
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    SidebarComponent,
    LinkComponent,
    RouterOutlet,
    NgIf
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit, OnDestroy {
  isMobile: boolean = false;
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
}
