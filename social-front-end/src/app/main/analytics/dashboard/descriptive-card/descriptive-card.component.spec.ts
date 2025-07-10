import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescriptiveCardComponent } from './descriptive-card.component';

describe('DescriptiveCardComponent', () => {
  let component: DescriptiveCardComponent;
  let fixture: ComponentFixture<DescriptiveCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescriptiveCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DescriptiveCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
