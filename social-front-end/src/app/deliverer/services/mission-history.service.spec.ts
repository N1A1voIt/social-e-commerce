import { TestBed } from '@angular/core/testing';

import { MissionHistoryService } from './mission-history.service';

describe('MissionHistoryService', () => {
  let service: MissionHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MissionHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
