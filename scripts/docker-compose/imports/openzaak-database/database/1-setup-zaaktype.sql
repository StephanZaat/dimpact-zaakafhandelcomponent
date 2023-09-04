CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO catalogi_catalogus (_admin_name, uuid, domein, rsin, contactpersoon_beheer_naam, contactpersoon_beheer_telefoonnummer, contactpersoon_beheer_emailadres, _etag) VALUES
    ('zac', '8225508a-6840-413e-acc9-6422af120db1', 'ALG', '002564440', 'ZAC Test Catalogus', '06-12345678', 'noreply@example.com', '_etag');

INSERT INTO authorizations_applicatie (uuid, client_ids, label, heeft_alle_autorisaties) VALUES (uuid_generate_v4(), '{zac_client}', 'ZAC', true);
-- Open Notificaties is not used yet in our Docker Compose set-up
-- INSERT INTO authorizations_applicatie (uuid, client_ids, label, heeft_alle_autorisaties) VALUES (uuid_generate_v4(), '{opennotificaties}', 'Open notificaties', true);
-- Open Formulieren is not used yet in our Docker Compose set-up
-- INSERT INTO authorizations_applicatie (uuid, client_ids, label, heeft_alle_autorisaties) VALUES (uuid_generate_v4(), '{openformulieren}', 'Open Formulieren', true);

INSERT INTO vng_api_common_jwtsecret (identifier, secret) VALUES ('zac_client', 'openzaakZaakafhandelcomponentClientSecret');
INSERT INTO vng_api_common_jwtsecret (identifier, secret) VALUES ('opennotificaties', 'opennotificaties');
INSERT INTO vng_api_common_jwtsecret (identifier, secret) VALUES ('openformulieren', 'openformulieren');

-- note that we currently use the public https://selectielijst.openzaak.nl/ VNG Selectielijst service here
INSERT INTO catalogi_zaaktype (id, datum_begin_geldigheid, datum_einde_geldigheid, concept, uuid, identificatie, zaaktype_omschrijving, zaaktype_omschrijving_generiek, vertrouwelijkheidaanduiding, doel, aanleiding, toelichting, indicatie_intern_of_extern, handeling_initiator, onderwerp, handeling_behandelaar, doorlooptijd_behandeling, servicenorm_behandeling, opschorting_en_aanhouding_mogelijk, verlenging_mogelijk, verlengingstermijn, trefwoorden, publicatie_indicatie, publicatietekst, verantwoordingsrelatie, versiedatum, producten_of_diensten, selectielijst_procestype, referentieproces_naam, referentieproces_link, catalogus_id, selectielijst_procestype_jaar, _etag) VALUES (1, '2021-01-01', NULL, false, '744ca059-f412-49d4-8963-5800e4afd486', 'bezwaar-behandelen', 'Bezwaar behandelen', 'Bezwaar behandelen', 'zaakvertrouwelijk', 'Een uitspraak doen op een ingekomen bezwaar tegen een eerder genomen besluit.', 'Er is een bezwaarschrift ontvangen tegen een besluit dat genomen is door de gemeente.', 'Conform de Algemene Wet Bestuursrecht (AWB) heeft een natuurlijk of niet-natuurlijk persoon de mogelijkheid om bezwaar te maken tegen een genomen besluit van de gemeente, bijvoorbeeld het niet verlenen van een vergunning.', 'extern', 'Indienen', 'Bezwaar', 'Behandelen', '84 days', NULL, false, true, '42 days', '{bezwaar,bezwaarschrift}', false, '', '{}', '2021-01-01', '{https://github.com/infonl/dimpact-zaakafhandelcomponent}', 'https://selectielijst.openzaak.nl/api/v1/procestypen/e1b73b12-b2f6-4c4e-8929-94f84dd2a57d', 'Bezwaar behandelen', 'http://www.gemmaonline.nl/index.php/Referentieproces_bezwaar_behandelen', 1, 2017, '_etag');

INSERT INTO catalogi_resultaattype (id, uuid, omschrijving, resultaattypeomschrijving, omschrijving_generiek, selectielijstklasse, archiefnominatie, archiefactietermijn, brondatum_archiefprocedure_afleidingswijze, brondatum_archiefprocedure_datumkenmerk, brondatum_archiefprocedure_einddatum_bekend, brondatum_archiefprocedure_objecttype, brondatum_archiefprocedure_registratie, brondatum_archiefprocedure_procestermijn, toelichting, zaaktype_id, _etag) VALUES (3, 'efe7a027-24a1-41f0-a044-c5a3a2f584f0', 'test', 'https://selectielijst.openzaak.nl/api/v1/resultaattypeomschrijvingen/3a0a9c3c-0847-4e7e-b7d9-765b9434094c', 'Gegrond', 'https://selectielijst.openzaak.nl/api/v1/resultaten/cc5ae4e3-a9e6-4386-bcee-46be4986a829', 'vernietigen', '10 years 0 mons 0 days 0 hours 0 mins 0.0 secs', 'afgehandeld', '', false, '', '', '0 years 0 mons 0 days 0 hours 0 mins 0.0 secs', 'test', 1, 'c397513bca29257bdf34b1212127917d');

INSERT INTO catalogi_statustype (id, uuid, statustype_omschrijving, statustype_omschrijving_generiek, statustypevolgnummer, informeren, statustekst, toelichting, zaaktype_id, _etag) VALUES (1, uuid_generate_v4(), 'Intake', 'Intake', 1, true, 'Geachte heer/mevrouw, Op %ZAAK. Registratiedatum% heeft u een bezwaar ingediend. Uw bezwaar is bij ons in behandeling onder zaaknummer %ZAAK.Zaakidentificatie%. Onlangs bent u al op de hoogte gesteld van het besluit. Met deze e-mail willen wij u mededelen dat het besluit per post naar u is toegestuurd en dat wij de zaak hebben afgesloten.', 'Het besluit is schriftelijk kenbaar gemaakt aan de indiener van het bezwaar. De zaak is gearchiveerd en afgehandeld.', 1, '_etag');
INSERT INTO catalogi_statustype (id, uuid, statustype_omschrijving, statustype_omschrijving_generiek, statustypevolgnummer, informeren, statustekst, toelichting, zaaktype_id, _etag) VALUES (2, uuid_generate_v4(), 'In behandeling', 'Zaak in behandeling', 2, true, 'Geachte heer/mevrouw, Uw zaak is in behandeling', 'Zaak is in behandeling', 1, '_etag');
INSERT INTO catalogi_statustype (id, uuid, statustype_omschrijving, statustype_omschrijving_generiek, statustypevolgnummer, informeren, statustekst, toelichting, zaaktype_id, _etag) VALUES (3, uuid_generate_v4(), 'Heropend', 'Zaak heropend', 3, true, 'Geachte heer/mevrouw, Uw zaak is in heropend', 'Zaak is heropend', 1, '_etag');
INSERT INTO catalogi_statustype (id, uuid, statustype_omschrijving, statustype_omschrijving_generiek, statustypevolgnummer, informeren, statustekst, toelichting, zaaktype_id, _etag) VALUES (4, uuid_generate_v4(), 'Afgerond', 'Zaak afgerond', 4, true, 'Geachte heer/mevrouw, Uw zaak met zaaknummer %ZAAK.Zaakidentificatie% is afgerond', 'Zaak is afgerond', 1, '_etag');

INSERT INTO catalogi_eigenschapspecificatie(id, groep, formaat, lengte, kardinaliteit, waardenverzameling) VALUES (1, 'tekst', 'tekst', 100, 1, '{}');

INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (1, '', uuid_generate_v4(), 'voornaam', 'voornaam', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (2, '', uuid_generate_v4(), 'achternaam', 'achternaam', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (3, '', uuid_generate_v4(), 'bsn', 'bsn', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (4, '', uuid_generate_v4(), 'bezwaar', 'bezwaar', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (5, '', uuid_generate_v4(), 'straatnaam', 'straatnaam', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (6, '', uuid_generate_v4(), 'huisnummer', 'huisnummer', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (7, '', uuid_generate_v4(), 'toevoeging', 'toevoeging', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (8, '', uuid_generate_v4(), 'postcode', 'postcode', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (9, '', uuid_generate_v4(), 'plaats', 'plaats', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (10, '', uuid_generate_v4(), 'telefoonnummer', 'telefoonnummer', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (11, '', uuid_generate_v4(), 'e-mailadres', 'e-mailadres', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (12, '', uuid_generate_v4(), 'zaaknummer', 'zaaknummer', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (13, '', uuid_generate_v4(), 'datumBesluit', 'datumBesluit', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (14, '', uuid_generate_v4(), 'besluit', 'besluit', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (15, '', uuid_generate_v4(), 'communicatie', 'communicatie', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (16, '', uuid_generate_v4(), 'beslissingBezwaar', 'beslissingBezwaar', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (17, '', uuid_generate_v4(), 'naamBehandelaar', 'naamBehandelaar', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (18, '', uuid_generate_v4(), 'e-mailBehandelaar', 'e-mailBehandelaar', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (19, '', uuid_generate_v4(), 'aanvraagAanvulInfo', 'aanvraagAanvulInfo', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (20, '', uuid_generate_v4(), 'aanvullendeInfo', 'aanvullendeInfo', 1, 1, '_etag');
INSERT INTO catalogi_eigenschap(id, toelichting, uuid, eigenschapnaam, definitie, specificatie_van_eigenschap_id, zaaktype_id, _etag) VALUES (21, '', uuid_generate_v4(), 'naamBeslisser', 'naamBeslisser', 1, 1, '_etag');

-- Note that these rol types must be known to ZAC as defined in the 'AardVanRol' Java enum in the ZAC code base.
INSERT INTO catalogi_roltype(id, uuid, omschrijving, omschrijving_generiek, zaaktype_id, _etag) VALUES (1, '1c359a1b-c38d-47b8-bed5-994db88ead61', 'Initiator', 'initiator', 1, '_etag');
INSERT INTO catalogi_roltype(id, uuid, omschrijving, omschrijving_generiek, zaaktype_id, _etag) VALUES (2, '1c359a1b-c38d-47b8-bed5-994db88ead62', 'Behandelaar', 'behandelaar', 1, '_etag');
INSERT INTO catalogi_roltype(id, uuid, omschrijving, omschrijving_generiek, zaaktype_id, _etag) VALUES (3, '1c359a1b-c38d-47b8-bed5-994db88ead63', 'Adviseur', 'adviseur', 1, '_etag');

-- ZAC required the informatie objecttype `e-mail` to be present (note the case sensitivity). Also see the 'ConfiguratieService.java' class in the ZAC code base.
INSERT INTO catalogi_informatieobjecttype(id, datum_begin_geldigheid, datum_einde_geldigheid, concept, uuid, omschrijving, vertrouwelijkheidaanduiding, catalogus_id, _etag) VALUES (1, '2021-10-04', NULL, false, 'efc332f2-be3b-4bad-9e3c-49a6219c92ad', 'e-mail', 'zaakvertrouwelijk', 1, '_etag');
INSERT INTO catalogi_informatieobjecttype(id, datum_begin_geldigheid, datum_einde_geldigheid, concept, uuid, omschrijving, vertrouwelijkheidaanduiding, catalogus_id, _etag) VALUES (2, '2021-10-04', NULL, false, 'bf3fcf61-2a1d-4f88-b6fc-6c58ac75f299', 'bijlage', 'zaakvertrouwelijk', 1, '_etag');

INSERT INTO catalogi_zaaktypeinformatieobjecttype(id, uuid, volgnummer, richting, informatieobjecttype_id, statustype_id, zaaktype_id, _etag) VALUES (1, '405da8a9-7296-439c-a2eb-a470b84f17ee', 1, 'inkomend', 1, NULL, 1, '_etag');
INSERT INTO catalogi_zaaktypeinformatieobjecttype(id, uuid, volgnummer, richting, informatieobjecttype_id, statustype_id, zaaktype_id, _etag) VALUES (2, 'eb80d8ad-de0a-460b-881a-107f3610a3b7', 2, 'inkomend', 2, NULL, 1, '_etag');

-- Open Notificaties is not used yet in our Docker Compose set-up
-- UPDATE notifications_notificationsconfig SET api_root = 'http://host.docker.internal:8002/api/v1/';

-- Open Formulieren is not used yet in our Docker Compose set-up
-- INSERT INTO zgw_consumers_service(label, api_type, api_root, client_id, secret, auth_type, header_key, header_value, oas, nlx, user_id, user_representation, oas_file) VALUES ('Open formulieren', 'nrc', 'http://host.docker.internal:8002/api/v1/', 'openzaak', 'openzaak', 'zgw', '', '', 'http://host.docker.internal:8002/api/v1/schema/openapi.yaml', '', '', '', '');
-- Set up the BAG service configuration. This requires that the corresponding variables have been passed on to this script.
INSERT INTO zgw_consumers_service(label, api_type, api_root, client_id, secret, auth_type, header_key, header_value, oas, nlx, user_id, user_representation, oas_file, client_certificate_id, server_certificate_id) VALUES ('BAG', 'orc', :'BAG_API_CLIENT_MP_REST_URL', '', '', 'api_key', 'X-Api-Key', :'BAG_API_KEY', 'https://api.bag.acceptatie.kadaster.nl/lvbag/individuelebevragingen/v2/', '', '', '', '', null, null);
