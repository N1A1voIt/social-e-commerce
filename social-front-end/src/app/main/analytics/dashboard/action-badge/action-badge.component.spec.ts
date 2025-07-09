import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionBadgeComponent } from './action-badge.component';

describe('ActionBadgeComponent', () => {
  let component: ActionBadgeComponent;
  let fixture: ComponentFixture<ActionBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActionBadgeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ActionBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
