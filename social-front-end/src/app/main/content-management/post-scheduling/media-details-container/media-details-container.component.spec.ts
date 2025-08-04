import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MediaDetailsContainerComponent } from './media-details-container.component';

describe('MediaDetailsContainerComponent', () => {
  let component: MediaDetailsContainerComponent;
  let fixture: ComponentFixture<MediaDetailsContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaDetailsContainerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MediaDetailsContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
