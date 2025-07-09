import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BestDealsCardComponent } from './best-deals-card.component';

describe('BestDealsCardComponent', () => {
  let component: BestDealsCardComponent;
  let fixture: ComponentFixture<BestDealsCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BestDealsCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BestDealsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
