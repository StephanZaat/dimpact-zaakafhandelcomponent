/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.zaken.converter;

import jakarta.inject.Inject;

import net.atos.client.zgw.zrc.ZRCClientService;
import net.atos.client.zgw.zrc.model.AardRelatie;
import net.atos.client.zgw.zrc.model.RelevanteZaak;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.generated.ZaakType;
import net.atos.zac.app.policy.converter.RESTRechtenConverter;
import net.atos.zac.app.zaken.model.RESTGerelateerdeZaak;
import net.atos.zac.app.zaken.model.RelatieType;
import net.atos.zac.policy.PolicyService;
import net.atos.zac.policy.output.ZaakRechten;

public class RESTGerelateerdeZaakConverter {

    @Inject
    private ZRCClientService zrcClientService;

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private RESTRechtenConverter rechtenConverter;

    @Inject
    private PolicyService policyService;

    public RESTGerelateerdeZaak convert(final Zaak zaak, final RelatieType relatieType) {
        final ZaakType zaaktype = ztcClientService.readZaaktype(zaak.getZaaktype());
        final ZaakRechten zaakrechten = policyService.readZaakRechten(zaak, zaaktype);
        final RESTGerelateerdeZaak restGerelateerdeZaak = new RESTGerelateerdeZaak();
        restGerelateerdeZaak.identificatie = zaak.getIdentificatie();
        restGerelateerdeZaak.relatieType = relatieType;
        restGerelateerdeZaak.rechten = rechtenConverter.convert(zaakrechten);
        if (zaakrechten.lezen()) {
            restGerelateerdeZaak.zaaktypeOmschrijving = zaaktype.getOmschrijving();
            restGerelateerdeZaak.startdatum = zaak.getStartdatum();
            if (zaak.getStatus() != null) {
                restGerelateerdeZaak.statustypeOmschrijving = ztcClientService.readStatustype(zrcClientService.readStatus(zaak.getStatus())
                        .getStatustype())
                        .getOmschrijving();
            }
        }
        return restGerelateerdeZaak;
    }

    public RESTGerelateerdeZaak convert(final RelevanteZaak relevanteZaak) {
        final Zaak zaak = zrcClientService.readZaak(relevanteZaak.getUrl());
        return convert(zaak, convertToRelatieType(relevanteZaak.getAardRelatie()));
    }

    public RelatieType convertToRelatieType(final AardRelatie aardRelatie) {
        return switch (aardRelatie) {
            case VERVOLG -> RelatieType.VERVOLG;
            case BIJDRAGE -> RelatieType.BIJDRAGE;
            case ONDERWERP -> RelatieType.ONDERWERP;
        };
    }
}
