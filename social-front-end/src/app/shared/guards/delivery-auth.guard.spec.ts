import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { deliveryAuthGuard } from './delivery-auth.guard';

describe('deliveryAuthGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => deliveryAuthGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
