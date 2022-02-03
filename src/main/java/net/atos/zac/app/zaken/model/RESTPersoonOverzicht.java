/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.zaken.model;

public class RESTPersoonOverzicht {

    public String bsn;

    public String naam;

    public String geboortedatum;

    public String inschrijfadres;

    public RESTPersoonOverzicht() {
    }

    public RESTPersoonOverzicht(final String bsn) {
        this.bsn = bsn;
    }
}
