/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { TestBed } from "@angular/core/testing";
import { FoutAfhandelingService } from "./fout-afhandeling.service";
import { HttpClientModule } from "@angular/common/http";

describe("FoutAfhandelingService", () => {
  let service: FoutAfhandelingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: FoutAfhandelingService, useValue: {} }],
      imports: [HttpClientModule],
    });

    service = TestBed.inject(FoutAfhandelingService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
