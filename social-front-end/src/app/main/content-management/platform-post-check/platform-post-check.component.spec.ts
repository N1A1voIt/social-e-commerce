import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformPostCheckComponent } from './platform-post-check.component';

describe('PlatformPostCheckComponent', () => {
  let component: PlatformPostCheckComponent;
  let fixture: ComponentFixture<PlatformPostCheckComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlatformPostCheckComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PlatformPostCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
