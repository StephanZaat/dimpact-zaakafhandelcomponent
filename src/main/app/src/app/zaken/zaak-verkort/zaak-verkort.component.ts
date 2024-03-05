/*
 * SPDX-FileCopyrightText: 2021 - 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { Component, Input, OnChanges } from "@angular/core";
import { UtilService } from "../../core/service/util.service";
import { Conditionals } from "../../shared/edit/conditional-fn";
import { TextIcon } from "../../shared/edit/text-icon";
import { Zaak } from "../model/zaak";

@Component({
  selector: "zac-zaak-verkort",
  templateUrl: "./zaak-verkort.component.html",
  styleUrls: ["./zaak-verkort.component.less"],
})
export class ZaakVerkortComponent implements OnChanges {
  @Input() zaak: Zaak;

  einddatumGeplandIcon: TextIcon;

  constructor(public utilService: UtilService) {}

  ngOnChanges(): void {
    this.einddatumGeplandIcon = new TextIcon(
      Conditionals.isAfterDate(this.zaak.einddatum),
      "report_problem",
      "warningZaakVerkortVerlopen_icon",
      "msg.datum.overschreden",
      "warning",
    );
  }
}
