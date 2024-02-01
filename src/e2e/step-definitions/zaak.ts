/*
 * SPDX-FileCopyrightText: 2023 Lifely
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { Then, When } from "@cucumber/cucumber";
import { CustomWorld } from "../support/worlds/world";
import fs from 'fs'

When("{string} wants to create a new zaak", { timeout: 60 * 1000 }, async function (this: CustomWorld, user) {
    await this.page.getByLabel("Zaak toevoegen").click();
    await this.page.getByLabel("Zaaktype").click();
    await this.page.getByRole("option", { name: "Melding evenement organiseren behandelen" }).click();
    await this.page
        .locator("div")
        .filter({ hasText: /^person$/ })
        .click();
    await this.page.getByLabel("BSN").click();
    await this.page.getByLabel("BSN").fill("999993896");
    await this.page.getByLabel("emoji_people Persoon").getByRole("button", { name: "Zoeken" }).click();
    await this.page.getByRole("button", { name: "Select" }).click();
    await this.page
        .locator("div")
        .filter({ hasText: /^gps_fixed$/ })
        .click();
    await this.page.getByPlaceholder("Zoeken op adres, postcode of woonplaats").click();
    await this.page.getByPlaceholder("Zoeken op adres, postcode of woonplaats").fill("1112gv");
    await this.page.getByPlaceholder("Zoeken op adres, postcode of woonplaats").press("Enter");
    await this.page
        .getByRole("row", { name: "Gerelateerde gegevens tonen 0384200000005901 Adres Meelbeskamp 49, 1112GV Diemen Selecteren" })
        .getByTitle('Selecteren')
        .click();
    await this.page.getByText('close').click();
    await this.page.getByLabel("Communicatiekanaal").click();
    await this.page.getByRole("option", { name: ' E-mail ' }).click();
    await this.page.waitForTimeout(1000)
    // Openbaar should be automatically selected on openbaar
    await this.page.getByText("Vertrouwelijkheidaanduiding- Openbaar -")
    await this.page.getByLabel("Omschrijving").click();
    await this.page.getByLabel("Omschrijving").fill("E2etest1");
    await this.page.getByRole("button", { name: "Aanmaken" }).click();

    await this.page.waitForTimeout(5000)

    const currentYear = new Date().getFullYear();

// Construct the regex pattern with the current year
    const regexPattern = new RegExp(`ZAAK-${currentYear}-\\d+`, 'g');

    await this.page.getByText(regexPattern)

        // Get the HTML content of the page
    const content = await this.page.content();

    // Find all matches
    const matches = content.match(regexPattern);

    if (matches && matches.length > 0) {
        console.log('saved case number: ', matches[0])
        this.testStorage.set('caseNumber', matches[0]);
    } else {
        throw new Error("No case number found");
    }

    await this.page.waitForTimeout(1000)
});

Then("{string} sees the created zaak", { timeout: 60 * 1000 }, async function (this: CustomWorld, user) {
    const caseNumber = this.testStorage.get('caseNumber');

    await this.page.getByText(caseNumber);
});


Then("{string} sees the created zaak with a delay", { timeout: 60 * 1000 + 15000 }, async function (this: CustomWorld, user) {
    // atleast a minute and 10 seconds just to be sure
    await this.page.waitForTimeout(60 * 1000 + 10000)
    const caseNumber = this.testStorage.get('caseNumber');

    await this.page.getByText(caseNumber);
});

Then("Employee {string} clicks on the first zaak in the zaak-werkvoorraad with delay", { timeout: 60 * 1000 + 15000 }, async function (this: CustomWorld, user) {
    await this.page.waitForTimeout(60 * 1000 + 10000)
    await this.page.reload();
    await this.page.getByText('visibility').first().click();
});

Then("Employee {string} sees the zaak that {string} created in open-forms", { timeout: 60 * 1000 }, async function (this: CustomWorld, user, profile) {
    const openFormsTestId = this.testStorage.get('open-forms-testid');

    await this.page.getByText('plagiarism').nth(1).click();
    await this.expect(this.page.getByAltText('Bijgevoegd document')).toBeVisible();

    
    await this.page.getByText('more_vert').first().click()
    const [download] = await Promise.all([
        this.page.waitForEvent('download'),
        this.page.getByText('Document downloaden').first().click()
    ]);

    const suggestedFileName = download.suggestedFilename();
    const filePath = 'ExportData/' + suggestedFileName;
    await download.saveAs(filePath);

    var pdf = require('pdf-parse');
    var dataBuffer = fs.readFileSync('./ExportData/' + suggestedFileName);
    await pdf(dataBuffer).then(function(data: any) {
        fs.writeFileSync('./ExportData/actual.txt', data.text);
    });

    let actual_export_values = fs.readFileSync('./ExportData/actual.txt', 'utf-8').replace(/(\r\n|\n|\r)/gm,"");
    this.expect(actual_export_values.includes(`Voornaam Alice:e2eid=${openFormsTestId}`)).toBe(true);
    this.expect(actual_export_values.includes('Voorletter(s) A')).toBe(true);
    this.expect(actual_export_values.includes('Tussenvoegsel(s) den')).toBe(true);
    this.expect(actual_export_values.includes('Achternaam Test')).toBe(true);
    this.expect(actual_export_values.includes('BSN BSN-nummer')).toBe(true);
    this.expect(actual_export_values.includes('Voornaam Demo')).toBe(true);
    this.expect(actual_export_values.includes('Voorletter(s)')).toBe(true);
    this.expect(actual_export_values.includes('Tussenvoegsel(s)')).toBe(true);
    this.expect(actual_export_values.includes('Achternaam Demo')).toBe(true);
    this.expect(actual_export_values.includes(`Omschrijving van het voorval Achterstallig onderhoudt aan de weg heeft schade aanmijn auto aangebracht`)).toBe(true);
    this.expect(actual_export_values.includes('Datum & tijdstip voorval 10 oktober 2024 00:00')).toBe(true);
    this.expect(actual_export_values.includes('materiële schade aan een voertuig')).toBe(true);
    this.expect(actual_export_values.includes('Waren er getuigen aanwezig ja')).toBe(true);
    this.expect(actual_export_values.includes('Hoeveel getuigen? 1')).toBe(true);
    this.expect(actual_export_values.includes(`Wilt u bijlagen meesturen met demelding? ja, digitaal bij deze melding`)).toBe(true);
    this.expect(actual_export_values.includes(`U kunt hier aangeven waar het voorval ongeveer heeft plaatsgevonden.Plaats Enschede`)).toBe(true);
    this.expect(actual_export_values.includes('Straat teststraat')).toBe(true);
    this.expect(actual_export_values.includes(`Nadere omschrijving van de locatie teststraat heeft behoorlijke gaten in de weg`)).toBe(true);
    this.expect(actual_export_values.includes(`U kunt hier aangeven waarom degemeente aansprakelijk is voor deschade: Achterstallig onderhoudt aan de weg`)).toBe(true);
    this.expect(actual_export_values.includes('Omschrijving schade voertuig klapband door gaten met scherpe randen in de weg')).toBe(true);
    this.expect(actual_export_values.includes('Merk voertuig CITROËN')).toBe(true);
    this.expect(actual_export_values.includes('Kenteken voertuig EE-RP-10')).toBe(true);
    this.expect(actual_export_values.includes('Bedrijfsnaam Verzekeraar bv')).toBe(true);
    this.expect(actual_export_values.includes('Polisnummer 111.111.111')).toBe(true);
    this.expect(actual_export_values.includes('Schade reeds gemeld ja')).toBe(true);
    this.expect(actual_export_values.includes('Hoe bent u verzekerd? AllRisk')).toBe(true);
    this.expect(actual_export_values.includes('Achternaam Test')).toBe(true);
    this.expect(actual_export_values.includes('Tussenvoegsels')).toBe(true);
    this.expect(actual_export_values.includes('Voornamen Robert')).toBe(true);
    this.expect(actual_export_values.includes('Postcode 1234 AB')).toBe(true);
    this.expect(actual_export_values.includes('Huisnummer')).toBe(true);
    this.expect(actual_export_values.includes('Foto bijlage: dent.jpg')).toBe(true);
    this.expect(actual_export_values.includes('Factuur of offerte bijlage: invoice.pdf')).toBe(true);
    this.expect(actual_export_values.includes('Andere documenten')).toBe(true);
});
