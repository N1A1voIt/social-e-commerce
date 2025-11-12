import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionFullComponent } from './transaction-full.component';

describe('TransactionFullComponent', () => {
  let component: TransactionFullComponent;
  let fixture: ComponentFixture<TransactionFullComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionFullComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransactionFullComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
