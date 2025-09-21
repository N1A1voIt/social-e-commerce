import { TestBed } from '@angular/core/testing';

import { DeliveryAuthService } from './delivery-auth.service';

describe('DeliveryAuthService', () => {
  let service: DeliveryAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeliveryAuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
