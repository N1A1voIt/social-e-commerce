import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

export interface NavigationItem {
  id: string;
  label: string;
  icon: string;
  route: string;
  isActive: boolean;
  children?: NavigationItem[];
}

export interface TreeNode {
  key: string;
  label: string;
  data: string;
  icon: string;
  children?: TreeNode[];
  selectable?: boolean;
  link?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NavigationDataService {
  private navigationItemsSubject = new BehaviorSubject<NavigationItem[]>([]);
  private treeDataSubject = new BehaviorSubject<TreeNode[]>([]);
  private activeRouteSubject = new BehaviorSubject<string>('');

  constructor(private router: Router) {
    this.initializeNavigationData();
    this.initializeTreeData();
    this.trackActiveRoute();
  }

  /**
   * Get navigation items as observable stream
   */
  getNavigationItems(): Observable<NavigationItem[]> {
    return this.navigationItemsSubject.asObservable();
  }

  /**
   * Get tree data as observable stream
   */
  getTreeData(): Observable<TreeNode[]> {
    return this.treeDataSubject.asObservable();
  }

  /**
   * Get current active route as observable stream
   */
  getActiveRoute(): Observable<string> {
    return this.activeRouteSubject.asObservable();
  }

  /**
   * Set active route and update navigation items
   */
  setActiveRoute(route: string): void {
    this.activeRouteSubject.next(route);
    this.updateActiveNavigationItems(route);
  }

  /**
   * Get current navigation items value (synchronous)
   */
  getCurrentNavigationItems(): NavigationItem[] {
    return this.navigationItemsSubject.value;
  }

  /**
   * Get current tree data value (synchronous)
   */
  getCurrentTreeData(): TreeNode[] {
    return this.treeDataSubject.value;
  }

  /**
   * Get current active route value (synchronous)
   */
  getCurrentActiveRoute(): string {
    return this.activeRouteSubject.value;
  }

  /**
   * Initialize navigation items based on app routes
   */
  private initializeNavigationData(): void {
    const navigationItems: NavigationItem[] = [
      {
        id: 'dashboard',
        label: 'Dashboard',
        icon: 'heroChartBarSquare',
        route: '/basic/dashboard',
        isActive: false
      },
      {
        id: 'content',
        label: 'Content Management',
        icon: 'heroDocumentSolid',
        route: '/basic/content',
        isActive: false
      },
      {
        id: 'feed',
        label: 'Feed',
        icon: 'heroDocumentSolid',
        route: '/basic/feed',
        isActive: false
      },
      {
        id: 'products',
        label: 'Products',
        icon: 'heroDocumentSolid',
        route: '/basic/products',
        isActive: false
      },
      {
        id: 'inbox',
        label: 'Inbox',
        icon: 'heroChatBubbleOvalLeftEllipsis',
        route: '/basic/inbox',
        isActive: false
      }
    ];

    this.navigationItemsSubject.next(navigationItems);
  }

  /**
   * Initialize tree data based on existing tree structure
   */
  private initializeTreeData(): void {
    const treeData: TreeNode[] = [
      {
        key: '0',
        label: 'Documents',
        data: 'Documents Folder',
        icon: 'pi pi-fw pi-inbox',
        children: [
          {
            key: '0-0',
            label: 'Work',
            data: 'Work Folder',
            icon: 'pi pi-fw pi-cog',
            children: [
              { 
                key: '0-0-0', 
                label: 'Expenses.doc', 
                icon: 'pi pi-fw pi-file', 
                data: 'Expenses Document',
                link: '/basic/dashboard'
              },
              { 
                key: '0-0-1', 
                label: 'Resume.doc', 
                icon: 'pi pi-fw pi-file', 
                data: 'Resume Document',
                link: '/basic/content'
              }
            ]
          },
          {
            key: '0-1',
            label: 'Home',
            data: 'Home Folder',
            icon: 'pi pi-fw pi-home',
            children: [
              { 
                key: '0-1-0', 
                label: 'Invoices.txt', 
                icon: 'pi pi-fw pi-file', 
                data: 'Invoices for this month',
                link: '/basic/products'
              }
            ]
          }
        ]
      },
      {
        key: '1',
        label: 'Events',
        data: 'Events Folder',
        icon: 'pi pi-fw pi-calendar',
        children: [
          { 
            key: '1-0', 
            label: 'Meeting', 
            icon: 'pi pi-fw pi-calendar-plus', 
            data: 'Meeting',
            link: '/basic/inbox'
          },
          { 
            key: '1-1', 
            label: 'Product Launch', 
            icon: 'pi pi-fw pi-calendar-plus', 
            data: 'Product Launch',
            link: '/basic/feed'
          },
          { 
            key: '1-2', 
            label: 'Report Review', 
            icon: 'pi pi-fw pi-calendar-plus', 
            data: 'Report Review',
            link: '/basic/dashboard'
          }
        ]
      },
      {
        key: '2',
        label: 'Movies',
        data: 'Movies Folder',
        icon: 'pi pi-fw pi-star-fill',
        children: [
          {
            key: '2-0',
            icon: 'pi pi-fw pi-star-fill',
            label: 'Al Pacino',
            data: 'Pacino Movies',
            children: [
              { 
                key: '2-0-0', 
                label: 'Scarface', 
                icon: 'pi pi-fw pi-video', 
                data: 'Scarface Movie',
                link: '/basic/content'
              },
              { 
                key: '2-0-1', 
                label: 'Serpico', 
                icon: 'pi pi-fw pi-video', 
                data: 'Serpico Movie',
                link: '/basic/products'
              }
            ]
          },
          {
            key: '2-1',
            label: 'Robert De Niro',
            icon: 'pi pi-fw pi-star-fill',
            data: 'De Niro Movies',
            children: [
              { 
                key: '2-1-0', 
                label: 'Goodfellas', 
                icon: 'pi pi-fw pi-video', 
                data: 'Goodfellas Movie',
                link: '/basic/feed'
              },
              { 
                key: '2-1-1', 
                label: 'Untouchables', 
                icon: 'pi pi-fw pi-video', 
                data: 'Untouchables Movie', 
                selectable: false,
                link: '/basic/inbox'
              }
            ]
          }
        ]
      }
    ];

    this.treeDataSubject.next(treeData);
  }

  /**
   * Track active route changes and update navigation state
   */
  private trackActiveRoute(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.setActiveRoute(event.url);
      });
  }

  /**
   * Update active state of navigation items based on current route
   */
  private updateActiveNavigationItems(activeRoute: string): void {
    const currentItems = this.navigationItemsSubject.value;
    const updatedItems = currentItems.map(item => ({
      ...item,
      isActive: item.route === activeRoute
    }));
    
    this.navigationItemsSubject.next(updatedItems);
  }
}