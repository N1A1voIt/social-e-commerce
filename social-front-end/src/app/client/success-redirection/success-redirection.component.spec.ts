import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SuccessRedirectionComponent } from './success-redirection.component';

describe('SuccessRedirectionComponent', () => {
  let component: SuccessRedirectionComponent;
  let fixture: ComponentFixture<SuccessRedirectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SuccessRedirectionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SuccessRedirectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
