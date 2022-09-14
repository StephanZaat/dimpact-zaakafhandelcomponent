/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.client.zgw.brc.model;

import static net.atos.zac.util.UriUtil.uuidFromURI;

import java.net.URI;
import java.util.UUID;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

/**
 *
 */
public class BesluitInformatieobject {

    /**
     * URL-referentie naar dit object.
     * Dit is de unieke identificatie en locatie van dit object.
     */
    private URI url;

    /**
     * Unieke resource identifier (UUID4)
     */
    private UUID uuid;

    /**
     * URL-referentie naar het INFORMATIEOBJECT (in de Documenten API), waar ook de relatieinformatie opgevraagd kan worden.
     */
    private URI informatieobject;

    /**
     * URL-referentie naar het BESLUIT.
     */
    private URI besluit;

    /**
     * Constructor for PATCH request
     */
    public BesluitInformatieobject() {
    }

    /**
     * Constructor with required attributes for POST and PUT requests
     */
    public BesluitInformatieobject(final URI informatieobject, final URI besluit) {
        this.informatieobject = informatieobject;
        this.besluit = besluit;
    }

    /**
     * Constructor with readOnly attributes for GET response
     */
    @JsonbCreator
    public BesluitInformatieobject(@JsonbProperty("url") final URI url,
                                   @JsonbProperty("uuid") final UUID uuid) {
        this.url = url;
        this.uuid = uuid;
    }

    public URI getUrl() {
        return url;
    }

    public UUID getUuid() {
        return uuid;
    }

    public URI getInformatieobject() {
        return informatieobject;
    }

    public void setInformatieobject(final URI informatieobject) {
        this.informatieobject = informatieobject;
    }

    public URI getBesluit() {
        return besluit;
    }

    public void setBesluit(final URI besluit) {
        this.besluit = besluit;
    }

    @JsonbTransient
    public UUID getBesluitUUID() {
        return uuidFromURI(besluit);
    }
}
