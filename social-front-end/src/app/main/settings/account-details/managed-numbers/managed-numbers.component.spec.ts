import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagedNumbersComponent } from './managed-numbers.component';

describe('ManagedNumbersComponent', () => {
  let component: ManagedNumbersComponent;
  let fixture: ComponentFixture<ManagedNumbersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManagedNumbersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ManagedNumbersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
