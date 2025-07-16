import { TestBed } from '@angular/core/testing';
import { Router, NavigationEnd } from '@angular/router';
import { Subject } from 'rxjs';
import { NavigationDataService, NavigationItem, TreeNode } from './navigation-data.service';

describe('NavigationDataService', () => {
  let service: NavigationDataService;
  let mockRouter: jasmine.SpyObj<Router>;
  let routerEventsSubject: Subject<any>;

  beforeEach(() => {
    routerEventsSubject = new Subject();
    mockRouter = jasmine.createSpyObj('Router', [], {
      events: routerEventsSubject.asObservable()
    });

    TestBed.configureTestingModule({
      providers: [
        NavigationDataService,
        { provide: Router, useValue: mockRouter }
      ]
    });
    
    service = TestBed.inject(NavigationDataService);
  });

  afterEach(() => {
    routerEventsSubject.complete();
  });

  describe('Service Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with navigation items', (done) => {
      service.getNavigationItems().subscribe(items => {
        expect(items).toBeDefined();
        expect(items.length).toBeGreaterThan(0);
        expect(items[0]).toHaveProperty('id');
        expect(items[0]).toHaveProperty('label');
        expect(items[0]).toHaveProperty('icon');
        expect(items[0]).toHaveProperty('route');
        expect(items[0]).toHaveProperty('isActive');
        done();
      });
    });

    it('should initialize with tree data', (done) => {
      service.getTreeData().subscribe(treeData => {
        expect(treeData).toBeDefined();
        expect(treeData.length).toBeGreaterThan(0);
        expect(treeData[0]).toHaveProperty('key');
        expect(treeData[0]).toHaveProperty('label');
        expect(treeData[0]).toHaveProperty('data');
        expect(treeData[0]).toHaveProperty('icon');
        done();
      });
    });

    it('should initialize with empty active route', (done) => {
      service.getActiveRoute().subscribe(route => {
        expect(route).toBe('');
        done();
      });
    });
  });

  describe('Navigation Items Management', () => {
    it('should return navigation items as observable', (done) => {
      service.getNavigationItems().subscribe(items => {
        expect(items).toBeInstanceOf(Array);
        expect(items.length).toBe(5); // Based on initialized data
        
        // Verify structure of navigation items
        const expectedItems = ['dashboard', 'content', 'feed', 'products', 'inbox'];
        items.forEach((item, index) => {
          expect(item.id).toBe(expectedItems[index]);
          expect(typeof item.label).toBe('string');
          expect(typeof item.icon).toBe('string');
          expect(typeof item.route).toBe('string');
          expect(typeof item.isActive).toBe('boolean');
        });
        done();
      });
    });

    it('should return current navigation items synchronously', () => {
      const items = service.getCurrentNavigationItems();
      expect(items).toBeInstanceOf(Array);
      expect(items.length).toBe(5);
    });

    it('should have correct navigation item structure', () => {
      const items = service.getCurrentNavigationItems();
      const dashboardItem = items.find(item => item.id === 'dashboard');
      
      expect(dashboardItem).toBeDefined();
      expect(dashboardItem!.label).toBe('Dashboard');
      expect(dashboardItem!.icon).toBe('heroChartBarSquare');
      expect(dashboardItem!.route).toBe('/basic/dashboard');
      expect(dashboardItem!.isActive).toBe(false);
    });
  });

  describe('Tree Data Management', () => {
    it('should return tree data as observable', (done) => {
      service.getTreeData().subscribe(treeData => {
        expect(treeData).toBeInstanceOf(Array);
        expect(treeData.length).toBe(3); // Documents, Events, Movies
        
        // Verify tree structure
        const documentsNode = treeData.find(node => node.label === 'Documents');
        expect(documentsNode).toBeDefined();
        expect(documentsNode!.children).toBeDefined();
        expect(documentsNode!.children!.length).toBeGreaterThan(0);
        done();
      });
    });

    it('should return current tree data synchronously', () => {
      const treeData = service.getCurrentTreeData();
      expect(treeData).toBeInstanceOf(Array);
      expect(treeData.length).toBe(3);
    });

    it('should have correct tree node structure', () => {
      const treeData = service.getCurrentTreeData();
      const documentsNode = treeData.find(node => node.label === 'Documents');
      
      expect(documentsNode).toBeDefined();
      expect(documentsNode!.key).toBe('0');
      expect(documentsNode!.data).toBe('Documents Folder');
      expect(documentsNode!.icon).toBe('pi pi-fw pi-inbox');
      expect(documentsNode!.children).toBeDefined();
      expect(documentsNode!.children!.length).toBe(2); // Work and Home folders
    });

    it('should have tree nodes with navigation links', () => {
      const treeData = service.getCurrentTreeData();
      const documentsNode = treeData[0];
      const workFolder = documentsNode.children![0];
      const expensesDoc = workFolder.children![0];
      
      expect(expensesDoc.link).toBe('/basic/dashboard');
      expect(expensesDoc.label).toBe('Expenses.doc');
    });
  });

  describe('Active Route Management', () => {
    it('should set and get active route', (done) => {
      const testRoute = '/basic/dashboard';
      
      service.setActiveRoute(testRoute);
      
      service.getActiveRoute().subscribe(route => {
        expect(route).toBe(testRoute);
        done();
      });
    });

    it('should return current active route synchronously', () => {
      const testRoute = '/basic/content';
      service.setActiveRoute(testRoute);
      
      const currentRoute = service.getCurrentActiveRoute();
      expect(currentRoute).toBe(testRoute);
    });

    it('should update navigation items when active route changes', (done) => {
      const testRoute = '/basic/dashboard';
      
      service.setActiveRoute(testRoute);
      
      service.getNavigationItems().subscribe(items => {
        const dashboardItem = items.find(item => item.route === testRoute);
        const otherItems = items.filter(item => item.route !== testRoute);
        
        expect(dashboardItem!.isActive).toBe(true);
        otherItems.forEach(item => {
          expect(item.isActive).toBe(false);
        });
        done();
      });
    });

    it('should handle multiple route changes correctly', () => {
      // Set initial route
      service.setActiveRoute('/basic/dashboard');
      let items = service.getCurrentNavigationItems();
      expect(items.find(item => item.route === '/basic/dashboard')!.isActive).toBe(true);
      
      // Change to different route
      service.setActiveRoute('/basic/content');
      items = service.getCurrentNavigationItems();
      expect(items.find(item => item.route === '/basic/content')!.isActive).toBe(true);
      expect(items.find(item => item.route === '/basic/dashboard')!.isActive).toBe(false);
    });
  });

  describe('Router Integration', () => {
    it('should track router navigation events', (done) => {
      const testUrl = '/basic/products';
      const navigationEndEvent = new NavigationEnd(1, testUrl, testUrl);
      
      // Subscribe to active route changes
      service.getActiveRoute().subscribe(route => {
        if (route === testUrl) {
          expect(route).toBe(testUrl);
          done();
        }
      });
      
      // Simulate router navigation
      routerEventsSubject.next(navigationEndEvent);
    });

    it('should update navigation items on router events', (done) => {
      const testUrl = '/basic/inbox';
      const navigationEndEvent = new NavigationEnd(1, testUrl, testUrl);
      
      // Subscribe to navigation items changes
      service.getNavigationItems().subscribe(items => {
        const inboxItem = items.find(item => item.route === testUrl);
        if (inboxItem && inboxItem.isActive) {
          expect(inboxItem.isActive).toBe(true);
          done();
        }
      });
      
      // Simulate router navigation
      routerEventsSubject.next(navigationEndEvent);
    });

    it('should ignore non-NavigationEnd router events', () => {
      const initialRoute = service.getCurrentActiveRoute();
      
      // Emit a non-NavigationEnd event
      routerEventsSubject.next({ type: 'SomeOtherEvent' });
      
      // Route should remain unchanged
      expect(service.getCurrentActiveRoute()).toBe(initialRoute);
    });
  });

  describe('Observable Streams', () => {
    it('should emit navigation items when subscribed', (done) => {
      let emissionCount = 0;
      
      service.getNavigationItems().subscribe(items => {
        emissionCount++;
        expect(items).toBeDefined();
        
        if (emissionCount === 1) {
          // Initial emission
          expect(items.length).toBe(5);
          
          // Trigger a change
          service.setActiveRoute('/basic/dashboard');
        } else if (emissionCount === 2) {
          // Second emission after route change
          expect(items.find(item => item.route === '/basic/dashboard')!.isActive).toBe(true);
          done();
        }
      });
    });

    it('should emit tree data when subscribed', (done) => {
      service.getTreeData().subscribe(treeData => {
        expect(treeData).toBeDefined();
        expect(treeData.length).toBe(3);
        done();
      });
    });

    it('should emit active route changes', (done) => {
      let emissionCount = 0;
      const routes = ['', '/basic/dashboard', '/basic/content'];
      
      service.getActiveRoute().subscribe(route => {
        expect(route).toBe(routes[emissionCount]);
        emissionCount++;
        
        if (emissionCount === 1) {
          service.setActiveRoute('/basic/dashboard');
        } else if (emissionCount === 2) {
          service.setActiveRoute('/basic/content');
        } else if (emissionCount === 3) {
          done();
        }
      });
    });
  });

  describe('Data Consistency', () => {
    it('should maintain consistent navigation data across multiple subscriptions', () => {
      const items1 = service.getCurrentNavigationItems();
      const items2 = service.getCurrentNavigationItems();
      
      expect(items1).toEqual(items2);
      expect(items1).not.toBe(items2); // Should be different instances
    });

    it('should maintain consistent tree data across multiple subscriptions', () => {
      const treeData1 = service.getCurrentTreeData();
      const treeData2 = service.getCurrentTreeData();
      
      expect(treeData1).toEqual(treeData2);
      expect(treeData1).not.toBe(treeData2); // Should be different instances
    });

    it('should synchronize active route across all methods', () => {
      const testRoute = '/basic/feed';
      
      service.setActiveRoute(testRoute);
      
      expect(service.getCurrentActiveRoute()).toBe(testRoute);
      
      // Verify through observable as well
      service.getActiveRoute().subscribe(route => {
        expect(route).toBe(testRoute);
      });
    });
  });
});