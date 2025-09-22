import { TestBed } from '@angular/core/testing';

import { IPopupService } from './i-popup.service';

describe('IPopupService', () => {
  let service: IPopupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IPopupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
