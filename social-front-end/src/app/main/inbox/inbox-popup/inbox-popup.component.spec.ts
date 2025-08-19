import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InboxPopupComponent } from './inbox-popup.component';

describe('InboxPopupComponent', () => {
  let component: InboxPopupComponent;
  let fixture: ComponentFixture<InboxPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InboxPopupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InboxPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
