import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MessageInputPsComponent } from './message-input-ps.component';

describe('MessageInputPsComponent', () => {
  let component: MessageInputPsComponent;
  let fixture: ComponentFixture<MessageInputPsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessageInputPsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MessageInputPsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
