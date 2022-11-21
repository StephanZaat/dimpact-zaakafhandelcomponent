/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {ZoekObject} from '../zoek-object';
import {ZaakRechten} from '../../../policy/model/zaak-rechten';
import {ZoekObjectType} from '../zoek-object-type';

export class ZaakZoekObject implements ZoekObject {
    id: string;
    type: ZoekObjectType;
    identificatie: string;
    omschrijving: string;
    toelichting: string;
    registratiedatum: string;
    startdatum: string;
    einddatumGepland: string;
    einddatum: string;
    uiterlijkeEinddatumAfdoening: string;
    publicatiedatum: string;
    communicatiekanaal: string;
    vertrouwelijkheidaanduiding: string;
    afgehandeld: boolean;
    groepNaam: string;
    behandelaarNaam: string;
    behandelaarGebruikersnaam: string;
    initiatorIdentificatie: string;
    locatie: string;
    indicatieVerlenging: boolean;
    duurVerlenging: string;
    redenVerlenging: string;
    indicatieOpschorting: boolean;
    redenOpschorting: string;
    zaaktypeUuid: string;
    zaaktypeOmschrijving: string;
    resultaattypeOmschrijving: string;
    resultaatToelichting: string;
    statustypeOmschrijving: string;
    indicatieDeelzaak: boolean;
    indicatieHoofdzaak: boolean;
    indicatieHeropend: boolean;
    statusToelichting: string;
    rechten: ZaakRechten;
}
