import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectResearchComponent } from './select-research.component';

describe('SelectResearchComponent', () => {
  let component: SelectResearchComponent;
  let fixture: ComponentFixture<SelectResearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectResearchComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SelectResearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
