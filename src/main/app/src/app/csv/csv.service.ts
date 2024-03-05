/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { catchError } from "rxjs/operators";
import { FoutAfhandelingService } from "../fout-afhandeling/fout-afhandeling.service";
import { ZoekParameters } from "../zoeken/model/zoek-parameters";

@Injectable({
  providedIn: "root",
})
export class CsvService {
  constructor(
    private http: HttpClient,
    private foutAfhandelingService: FoutAfhandelingService,
  ) {}

  private basepath = "/rest/csv";

  exportToCSV(zoekParameters: ZoekParameters): Observable<Blob> {
    return this.http
      .post(`${this.basepath}/export`, zoekParameters, { responseType: "blob" })
      .pipe(
        catchError((err) => this.foutAfhandelingService.foutAfhandelen(err)),
      );
  }
}
