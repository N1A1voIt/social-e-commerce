import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviousMissionComponent } from './previous-mission.component';

describe('PreviousMissionComponent', () => {
  let component: PreviousMissionComponent;
  let fixture: ComponentFixture<PreviousMissionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PreviousMissionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PreviousMissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
