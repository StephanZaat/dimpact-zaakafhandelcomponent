/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.informatieobjecten.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.ws.rs.FormParam;

import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObject;
import net.atos.zac.app.configuratie.model.RESTTaal;


public class RESTEnkelvoudigInformatieObjectVersieGegevens {

    @FormParam("uuid")
    public UUID uuid;

    @FormParam("zaakUuid")
    public UUID zaakUuid;

    @FormParam("titel")
    public String titel;

    @FormParam("vertrouwelijkheidaanduiding")
    public String vertrouwelijkheidaanduiding;

    @FormParam("auteur")
    public String auteur;

    @FormParam("status")
    public EnkelvoudigInformatieObject.StatusEnum status;

    @FormParam("taal")
    public RESTTaal taal;

    @FormParam("bestandsnaam")
    public String bestandsnaam;

    @FormParam("formaat")
    public String formaat;

    @FormParam("file")
    public byte[] file;

    @FormParam("beschrijving")
    public String beschrijving;

    @FormParam("verzenddatum")
    public LocalDate verzenddatum;

    @FormParam("ontvangstdatum")
    public LocalDate ontvangstdatum;

    @FormParam("toelichting")
    public String toelichting;
}
