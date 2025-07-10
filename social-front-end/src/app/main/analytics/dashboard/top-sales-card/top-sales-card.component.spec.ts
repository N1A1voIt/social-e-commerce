import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopSalesCardComponent } from './top-sales-card.component';

describe('TopSalesCardComponent', () => {
  let component: TopSalesCardComponent;
  let fixture: ComponentFixture<TopSalesCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopSalesCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TopSalesCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
