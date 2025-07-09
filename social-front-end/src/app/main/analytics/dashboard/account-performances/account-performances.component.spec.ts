import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountPerformancesComponent } from './account-performances.component';

describe('AccountPerformancesComponent', () => {
  let component: AccountPerformancesComponent;
  let fixture: ComponentFixture<AccountPerformancesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountPerformancesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AccountPerformancesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
