import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContectRowComponent } from './contect-row.component';

describe('ContectRowComponent', () => {
  let component: ContectRowComponent;
  let fixture: ComponentFixture<ContectRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContectRowComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ContectRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
