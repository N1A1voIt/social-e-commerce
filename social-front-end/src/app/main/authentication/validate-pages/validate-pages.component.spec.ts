import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidatePagesComponent } from './validate-pages.component';

describe('ValidatePagesComponent', () => {
  let component: ValidatePagesComponent;
  let fixture: ComponentFixture<ValidatePagesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidatePagesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ValidatePagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
