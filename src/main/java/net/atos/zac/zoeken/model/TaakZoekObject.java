/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.zoeken.model;

import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

public class TaakZoekObject implements ZoekObject {

    @Field
    private String uuid;

    @Field
    private String type;

    @Field
    private String identificatie;

    @Field("taak_naam")
    private String naam;

    @Field("taak_toelichting")
    private String toelichting;

    @Field("taak_status")
    private String status;

    @Field("taak_zaaktypeUuid")
    private String zaaktypeUuid;

    @Field("taak_zaaktypeIdentificatie")
    private String zaaktypeIdentificatie;

    @Field("taak_zaaktypeOmschrijving")
    private String zaaktypeOmschrijving;

    @Field("taak_zaakUuid")
    private String zaakUUID;

    @Field("taak_zaakId")
    private String zaakID;

    @Field("taak_creatiedatum")
    private Date creatiedatum;

    @Field("taak_toekenningsdatum")
    private Date toekenningsdatum;

    @Field("taak_streefdatum")
    private Date streefdatum;

    @Field("taak_groepId")
    private String groepID;

    @Field("taak_groepNaam")
    private String groepNaam;

    @Field("taak_behandelaarNaam")
    private String behandelaarNaam;

    @Field("taak_behandelaarGebruikersnaam")
    private String behandelaarGebruikersnaam;

    @Field("taak_data")
    private List<String> taakData;

    @Field("taak_informatie")
    private List<String> taakInformatie;


    public TaakZoekObject() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getIdentificatie() {
        return identificatie;
    }

    public void setIdentificatie(final String identificatie) {
        this.identificatie = identificatie;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(final String naam) {
        this.naam = naam;
    }

    public String getToelichting() {
        return toelichting;
    }

    public void setToelichting(final String toelichting) {
        this.toelichting = toelichting;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getZaaktypeOmschrijving() {
        return zaaktypeOmschrijving;
    }

    public void setZaaktypeOmschrijving(final String zaaktypeOmschrijving) {
        this.zaaktypeOmschrijving = zaaktypeOmschrijving;
    }

    public String getZaaktypeUuid() {
        return zaaktypeUuid;
    }

    public void setZaaktypeUuid(final String zaaktypeUuid) {
        this.zaaktypeUuid = zaaktypeUuid;
    }

    public String getZaaktypeIdentificatie() {
        return zaaktypeIdentificatie;
    }

    public void setZaaktypeIdentificatie(final String zaaktypeIdentificatie) {
        this.zaaktypeIdentificatie = zaaktypeIdentificatie;
    }

    public String getZaakUUID() {
        return zaakUUID;
    }

    public void setZaakUUID(final String zaakUUID) {
        this.zaakUUID = zaakUUID;
    }

    public String getZaakID() {
        return zaakID;
    }

    public void setZaakID(final String zaakID) {
        this.zaakID = zaakID;
    }

    public Date getCreatiedatum() {
        return creatiedatum;
    }

    public void setCreatiedatum(final Date creatiedatum) {
        this.creatiedatum = creatiedatum;
    }

    public Date getToekenningsdatum() {
        return toekenningsdatum;
    }

    public void setToekenningsdatum(final Date toekenningsdatum) {
        this.toekenningsdatum = toekenningsdatum;
    }

    public Date getStreefdatum() {
        return streefdatum;
    }

    public void setStreefdatum(final Date streefdatum) {
        this.streefdatum = streefdatum;
    }

    public String getGroepID() {
        return groepID;
    }

    public void setGroepID(final String groepID) {
        this.groepID = groepID;
    }

    public String getGroepNaam() {
        return groepNaam;
    }

    public void setGroepNaam(final String groepNaam) {
        this.groepNaam = groepNaam;
    }

    public String getBehandelaarNaam() {
        return behandelaarNaam;
    }

    public void setBehandelaarNaam(final String behandelaarNaam) {
        this.behandelaarNaam = behandelaarNaam;
    }

    public String getBehandelaarGebruikersnaam() {
        return behandelaarGebruikersnaam;
    }

    public void setBehandelaarGebruikersnaam(final String behandelaarGebruikersnaam) {
        this.behandelaarGebruikersnaam = behandelaarGebruikersnaam;
    }

    public List<String> getTaakData() {
        return taakData;
    }

    public void setTaakData(final List<String> taakData) {
        this.taakData = taakData;
    }

    public List<String> getTaakInformatie() {
        return taakInformatie;
    }

    public void setTaakInformatie(final List<String> taakInformatie) {
        this.taakInformatie = taakInformatie;
    }
}
