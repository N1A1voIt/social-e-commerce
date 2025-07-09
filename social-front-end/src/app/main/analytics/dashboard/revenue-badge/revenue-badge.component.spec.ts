import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RevenueBadgeComponent } from './revenue-badge.component';

describe('RevenueBadgeComponent', () => {
  let component: RevenueBadgeComponent;
  let fixture: ComponentFixture<RevenueBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RevenueBadgeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RevenueBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
