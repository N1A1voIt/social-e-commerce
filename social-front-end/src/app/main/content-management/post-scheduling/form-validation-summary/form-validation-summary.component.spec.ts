import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormValidationSummaryComponent } from './form-validation-summary.component';

describe('FormValidationSummaryComponent', () => {
  let component: FormValidationSummaryComponent;
  let fixture: ComponentFixture<FormValidationSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormValidationSummaryComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(FormValidationSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
