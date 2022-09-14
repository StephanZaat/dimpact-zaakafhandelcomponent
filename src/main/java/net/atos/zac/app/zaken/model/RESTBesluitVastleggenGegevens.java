/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.zaken.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RESTBesluitVastleggenGegevens {

    public UUID zaakUuid;

    public UUID resultaattypeUuid;

    public UUID besluittypeUuid;

    public String toelichting;

    public LocalDate ingangsdatum;

    public LocalDate vervaldatum;

    public List<String> documenten;
}
