import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostSchedulingComponent } from './post-scheduling.component';

describe('PostSchedulingComponent', () => {
  let component: PostSchedulingComponent;
  let fixture: ComponentFixture<PostSchedulingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostSchedulingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PostSchedulingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
