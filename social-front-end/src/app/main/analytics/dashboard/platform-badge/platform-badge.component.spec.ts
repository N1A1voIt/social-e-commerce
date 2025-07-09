import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformBadgeComponent } from './platform-badge.component';

describe('PlatformBadgeComponent', () => {
  let component: PlatformBadgeComponent;
  let fixture: ComponentFixture<PlatformBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlatformBadgeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PlatformBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
