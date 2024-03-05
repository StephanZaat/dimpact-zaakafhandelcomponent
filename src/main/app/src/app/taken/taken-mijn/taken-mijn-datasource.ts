/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { UtilService } from "../../core/service/util.service";
import { Werklijst } from "../../gebruikersvoorkeuren/model/werklijst";
import { ZoekenDataSource } from "../../shared/dynamic-table/datasource/zoeken-data-source";
import { TaakZoekObject } from "../../zoeken/model/taken/taak-zoek-object";
import { ZoekObjectType } from "../../zoeken/model/zoek-object-type";
import { ZoekParameters } from "../../zoeken/model/zoek-parameters";
import { ZoekenService } from "../../zoeken/zoeken.service";

export class TakenMijnDatasource extends ZoekenDataSource<TaakZoekObject> {
  constructor(zoekenService: ZoekenService, utilService: UtilService) {
    super(Werklijst.MIJN_TAKEN, zoekenService, utilService);
  }

  protected initZoekparameters(zoekParameters: ZoekParameters) {
    TakenMijnDatasource.mijnLopendeTaken(zoekParameters);
  }

  public static mijnLopendeTaken(
    zoekParameters: ZoekParameters,
  ): ZoekParameters {
    zoekParameters.type = ZoekObjectType.TAAK;
    zoekParameters.alleenMijnTaken = true;
    return zoekParameters;
  }
}
