import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MainMessageComponent } from './main-message.component';

describe('MainMessageComponent', () => {
  let component: MainMessageComponent;
  let fixture: ComponentFixture<MainMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainMessageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MainMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
