import { TestBed } from '@angular/core/testing';

import { DauthService } from './dauth.service';

describe('DauthService', () => {
  let service: DauthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DauthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
