import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { CatalogueService } from './catalogue';

describe('CatalogueService', () => {
  let service: CatalogueService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(CatalogueService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
