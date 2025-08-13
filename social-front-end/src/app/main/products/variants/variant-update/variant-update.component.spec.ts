import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VariantUpdateComponent } from './variant-update.component';

describe('VariantUpdateComponent', () => {
  let component: VariantUpdateComponent;
  let fixture: ComponentFixture<VariantUpdateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VariantUpdateComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VariantUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
