/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { IdentificatieType } from "../klanten/identificatieType";
import { Klant } from "../klanten/klant";

export class Persoon implements Klant {
  bsn: string;
  geslacht: string;
  naam: string;
  geboortedatum: string;
  verblijfplaats: string;
  identificatieType: IdentificatieType;
  identificatie: string;
  emailadres: string;
  telefoonnummer: string;
}
