import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MvolaComponent } from './mvola.component';

describe('MvolaComponent', () => {
  let component: MvolaComponent;
  let fixture: ComponentFixture<MvolaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MvolaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MvolaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
