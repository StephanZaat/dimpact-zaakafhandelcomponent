/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.informatieobjecten;

import static net.atos.client.zgw.shared.util.InformatieobjectenUtil.convertByteArrayToBase64String;
import static net.atos.client.zgw.shared.util.URIUtil.parseUUIDFromResourceURI;
import static net.atos.zac.app.informatieobjecten.converter.RESTInformatieobjectConverter.convertToEnkelvoudigInformatieObject;
import static net.atos.zac.configuratie.ConfiguratieService.INFORMATIEOBJECTTYPE_OMSCHRIJVING_BIJLAGE;
import static net.atos.zac.configuratie.ConfiguratieService.OMSCHRIJVING_VOORWAARDEN_GEBRUIKSRECHTEN;
import static net.atos.zac.policy.PolicyService.assertPolicy;
import static net.atos.zac.websocket.event.ScreenEventType.ENKELVOUDIG_INFORMATIEOBJECT;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.flowable.task.api.Task;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import net.atos.client.officeconverter.OfficeConverterClientService;
import net.atos.client.zgw.drc.DRCClientService;
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObject;
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObjectData;
import net.atos.client.zgw.drc.model.generated.EnkelvoudigInformatieObjectWithLockData;
import net.atos.client.zgw.shared.ZGWApiService;
import net.atos.client.zgw.shared.model.audit.AuditTrailRegel;
import net.atos.client.zgw.zrc.ZRCClientService;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.client.zgw.zrc.model.ZaakInformatieobject;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.generated.BesluitType;
import net.atos.client.zgw.ztc.model.generated.InformatieObjectType;
import net.atos.client.zgw.ztc.model.generated.ZaakType;
import net.atos.client.zgw.ztc.util.InformatieObjectTypeUtil;
import net.atos.zac.app.audit.converter.RESTHistorieRegelConverter;
import net.atos.zac.app.audit.model.RESTHistorieRegel;
import net.atos.zac.app.informatieobjecten.converter.RESTInformatieobjectConverter;
import net.atos.zac.app.informatieobjecten.converter.RESTInformatieobjecttypeConverter;
import net.atos.zac.app.informatieobjecten.converter.RESTZaakInformatieobjectConverter;
import net.atos.zac.app.informatieobjecten.model.RESTDocumentCreatieGegevens;
import net.atos.zac.app.informatieobjecten.model.RESTDocumentCreatieResponse;
import net.atos.zac.app.informatieobjecten.model.RESTDocumentVerplaatsGegevens;
import net.atos.zac.app.informatieobjecten.model.RESTDocumentVerwijderenGegevens;
import net.atos.zac.app.informatieobjecten.model.RESTDocumentVerzendGegevens;
import net.atos.zac.app.informatieobjecten.model.RESTEnkelvoudigInformatieObjectVersieGegevens;
import net.atos.zac.app.informatieobjecten.model.RESTEnkelvoudigInformatieobject;
import net.atos.zac.app.informatieobjecten.model.RESTFileUpload;
import net.atos.zac.app.informatieobjecten.model.RESTGekoppeldeZaakEnkelvoudigInformatieObject;
import net.atos.zac.app.informatieobjecten.model.RESTInformatieobjectZoekParameters;
import net.atos.zac.app.informatieobjecten.model.RESTInformatieobjecttype;
import net.atos.zac.app.informatieobjecten.model.RESTZaakInformatieobject;
import net.atos.zac.app.zaken.converter.RESTGerelateerdeZaakConverter;
import net.atos.zac.app.zaken.model.RelatieType;
import net.atos.zac.authentication.ActiveSession;
import net.atos.zac.authentication.LoggedInUser;
import net.atos.zac.documentcreatie.DocumentCreatieService;
import net.atos.zac.documentcreatie.model.DocumentCreatieGegevens;
import net.atos.zac.documentcreatie.model.DocumentCreatieResponse;
import net.atos.zac.documenten.InboxDocumentenService;
import net.atos.zac.documenten.OntkoppeldeDocumentenService;
import net.atos.zac.documenten.model.InboxDocument;
import net.atos.zac.documenten.model.OntkoppeldDocument;
import net.atos.zac.enkelvoudiginformatieobject.EnkelvoudigInformatieObjectLockService;
import net.atos.zac.event.EventingService;
import net.atos.zac.flowable.TaakVariabelenService;
import net.atos.zac.flowable.TakenService;
import net.atos.zac.policy.PolicyService;
import net.atos.zac.util.UriUtil;
import net.atos.zac.webdav.WebdavHelper;

@Singleton
@Path("informatieobjecten")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InformatieObjectenRESTService {
    private static final String MEDIA_TYPE_PDF = "application/pdf";

    private static final String TOELICHTING_PDF = "Geconverteerd naar PDF";

    static final String FILE_SESSION_ATTRIBUTE_PREFIX = "FILE_";

    @Inject
    private DRCClientService drcClientService;

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private ZRCClientService zrcClientService;

    @Inject
    private ZGWApiService zgwApiService;

    @Inject
    private TakenService takenService;

    @Inject
    private TaakVariabelenService taakVariabelenService;

    @Inject
    private OntkoppeldeDocumentenService ontkoppeldeDocumentenService;

    @Inject
    private InboxDocumentenService inboxDocumentenService;

    @Inject
    private EnkelvoudigInformatieObjectLockService enkelvoudigInformatieObjectLockService;

    @Inject
    private EventingService eventingService;

    @Inject
    private RESTZaakInformatieobjectConverter zaakInformatieobjectConverter;

    @Inject
    private RESTInformatieobjectConverter informatieobjectConverter;

    @Inject
    private RESTInformatieobjecttypeConverter informatieobjecttypeConverter;

    @Inject
    private RESTHistorieRegelConverter historieRegelConverter;

    @Inject
    private RESTGerelateerdeZaakConverter gerelateerdeZaakConverter;

    @Inject
    private DocumentCreatieService documentCreatieService;

    @Inject
    private Instance<LoggedInUser> loggedInUserInstance;

    @Inject
    private WebdavHelper webdavHelper;

    @Inject
    @ActiveSession
    private Instance<HttpSession> httpSession;

    @Inject
    private PolicyService policyService;

    @Inject
    private EnkelvoudigInformatieObjectDownloadService enkelvoudigInformatieObjectDownloadService;

    @Inject
    private EnkelvoudigInformatieObjectUpdateService enkelvoudigInformatieObjectUpdateService;

    @Inject
    private OfficeConverterClientService officeConverterClientService;

    @GET
    @Path("informatieobject/{uuid}")
    public RESTEnkelvoudigInformatieobject readEnkelvoudigInformatieobject(
            @PathParam("uuid") final UUID uuid,
            @QueryParam("zaak") final UUID zaakUUID
    ) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieObject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        final RESTEnkelvoudigInformatieobject restEnkelvoudigInformatieobject;
        if (zaakUUID != null) {
            restEnkelvoudigInformatieobject = informatieobjectConverter.convertToREST(enkelvoudigInformatieObject,
                    zrcClientService.readZaak(
                            zaakUUID));
        } else {
            restEnkelvoudigInformatieobject = informatieobjectConverter.convertToREST(enkelvoudigInformatieObject);
        }
        return restEnkelvoudigInformatieobject;
    }

    @GET
    @Path("informatieobject/versie/{uuid}/{versie}")
    public RESTEnkelvoudigInformatieobject readEnkelvoudigInformatieobject(
            @PathParam("uuid") final UUID uuid,
            @PathParam("versie") final int versie
    ) {
        final EnkelvoudigInformatieObject huidigeVersie = drcClientService.readEnkelvoudigInformatieobject(uuid);
        final RESTEnkelvoudigInformatieobject restEnkelvoudigInformatieobject;
        if (versie < huidigeVersie.getVersie()) {
            restEnkelvoudigInformatieobject = informatieobjectConverter.convertToREST(
                    drcClientService.readEnkelvoudigInformatieobjectVersie(uuid, versie));
        } else {
            restEnkelvoudigInformatieobject = informatieobjectConverter.convertToREST(huidigeVersie);
        }
        return restEnkelvoudigInformatieobject;
    }

    @PUT
    @Path("informatieobjectenList")
    public List<RESTEnkelvoudigInformatieobject> listEnkelvoudigInformatieobjecten(
            final RESTInformatieobjectZoekParameters zoekParameters
    ) {
        final Zaak zaak = zoekParameters.zaakUUID != null ? zrcClientService.readZaak(zoekParameters.zaakUUID) : null;
        if (zoekParameters.informatieobjectUUIDs != null) {
            return informatieobjectConverter.convertUUIDsToREST(zoekParameters.informatieobjectUUIDs, zaak);
        } else if (zaak != null) {
            assertPolicy(policyService.readZaakRechten(zaak).lezen());
            List<RESTEnkelvoudigInformatieobject> enkelvoudigInformatieobjectenVoorZaak = listEnkelvoudigInformatieobjectenVoorZaak(
                    zaak);
            if (zoekParameters.gekoppeldeZaakDocumenten) {
                enkelvoudigInformatieobjectenVoorZaak = new ArrayList<>(enkelvoudigInformatieobjectenVoorZaak);
                enkelvoudigInformatieobjectenVoorZaak.addAll(listGekoppeldeZaakInformatieObjectenVoorZaak(zaak));
            }
            if (zoekParameters.besluittypeUUID != null) {
                final BesluitType besluittype = ztcClientService.readBesluittype(zoekParameters.besluittypeUUID);
                final List<UUID> compareList = besluittype.getInformatieobjecttypen().stream().map(UriUtil::uuidFromURI)
                        .toList();
                return enkelvoudigInformatieobjectenVoorZaak.stream()
                        .filter(enkelvoudigInformatieObject -> compareList.contains(
                                enkelvoudigInformatieObject.informatieobjectTypeUUID))
                        .toList();
            } else {
                return enkelvoudigInformatieobjectenVoorZaak;
            }
        } else {
            throw new IllegalStateException("Zoekparameters hebben geen waarde");
        }
    }

    @GET
    @Path("informatieobjecten/zaak/{zaakUuid}/teVerzenden")
    public List<RESTEnkelvoudigInformatieobject> listEnkelvoudigInformatieobjectenVoorVerzenden(
            @PathParam("zaakUuid") final UUID zaakUuid
    ) {
        final Zaak zaak = zrcClientService.readZaak(zaakUuid);
        assertPolicy(policyService.readZaakRechten(zaak).lezen());
        return zrcClientService.listZaakinformatieobjecten(zaak).stream()
                .map(zaakinformatieobject -> drcClientService.readEnkelvoudigInformatieobject(
                        zaakinformatieobject.getInformatieobject()))
                .filter(this::isVerzendenToegestaan)
                .map(informatieobject -> informatieobjectConverter.convertToREST(informatieobject, zaak))
                .collect(Collectors.toList());
    }

    @POST
    @Path("informatieobjecten/verzenden")
    public void verzenden(final RESTDocumentVerzendGegevens gegevens) {
        final List<EnkelvoudigInformatieObject> informatieobjecten = gegevens.informatieobjecten.stream()
                .map(uuid -> drcClientService.readEnkelvoudigInformatieobject(uuid)).toList();
        final Zaak zaak = zrcClientService.readZaak(gegevens.zaakUuid);
        assertPolicy(policyService.readZaakRechten(zaak).wijzigen());
        informatieobjecten.forEach(informatieobject -> assertPolicy(isVerzendenToegestaan(informatieobject)));
        informatieobjecten.forEach(
                informatieobject -> enkelvoudigInformatieObjectUpdateService.verzendEnkelvoudigInformatieObject(
                        parseUUIDFromResourceURI(informatieobject.getUrl()),
                        gegevens.verzenddatum,
                        gegevens.toelichting
                )
        );
    }

    @POST
    @Path("informatieobject/{zaakUuid}/{documentReferentieId}")
    public RESTEnkelvoudigInformatieobject createEnkelvoudigInformatieobjectWithUploadedFile(
            @PathParam("zaakUuid") final UUID zaakUuid,
            @PathParam("documentReferentieId") final String documentReferentieId,
            @QueryParam("taakObject") final boolean taakObject,
            @Valid final RESTEnkelvoudigInformatieobject restEnkelvoudigInformatieobject
    ) {
        final Zaak zaak = zrcClientService.readZaak(zaakUuid);
        assertPolicy(policyService.readZaakRechten(zaak).wijzigen());

        final RESTFileUpload file = (RESTFileUpload) httpSession.get().getAttribute(FILE_SESSION_ATTRIBUTE_PREFIX +
                                                                                    documentReferentieId);

        try {
            final EnkelvoudigInformatieObjectData enkelvoudigInformatieObjectData = taakObject ?
                    informatieobjectConverter.convertTaakObject(restEnkelvoudigInformatieobject, file) :
                    informatieobjectConverter.convertZaakObject(restEnkelvoudigInformatieobject, file);
            return createEnkelvoudigInformatieobject(enkelvoudigInformatieObjectData, documentReferentieId, taakObject, zaak);
        } finally {
            // always remove the uploaded file from the HTTP session even if exceptions are thrown
            httpSession.get().removeAttribute(FILE_SESSION_ATTRIBUTE_PREFIX + documentReferentieId);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("informatieobject/{zaakUuid}/{documentReferentieId}")
    public RESTEnkelvoudigInformatieobject createEnkelvoudigInformatieobjectAndUploadFile(
            @PathParam("zaakUuid") final UUID zaakUuid,
            @PathParam("documentReferentieId") final String documentReferentieId,
            @QueryParam("taakObject") final boolean taakObject,
            @Valid @MultipartForm final RESTEnkelvoudigInformatieobject restEnkelvoudigInformatieobject
    ) {
        final Zaak zaak = zrcClientService.readZaak(zaakUuid);
        assertPolicy(policyService.readZaakRechten(zaak).wijzigen());

        final EnkelvoudigInformatieObjectData enkelvoudigInformatieObjectData = taakObject ?
                informatieobjectConverter.convertTaakObject(restEnkelvoudigInformatieobject) :
                informatieobjectConverter.convertZaakObject(restEnkelvoudigInformatieobject);
        return createEnkelvoudigInformatieobject(enkelvoudigInformatieObjectData, documentReferentieId, taakObject, zaak);
    }

    private RESTEnkelvoudigInformatieobject createEnkelvoudigInformatieobject(
            EnkelvoudigInformatieObjectData enkelvoudigInformatieObjectData,
            String documentReferentieId,
            boolean taakObject,
            Zaak zaak
    ) {
        final ZaakInformatieobject zaakInformatieobject = zgwApiService.createZaakInformatieobjectForZaak(
                zaak,
                enkelvoudigInformatieObjectData,
                enkelvoudigInformatieObjectData.getTitel(),
                enkelvoudigInformatieObjectData.getBeschrijving(),
                OMSCHRIJVING_VOORWAARDEN_GEBRUIKSRECHTEN
        );
        if (taakObject) {
            final Task task = takenService.findOpenTask(documentReferentieId);
            if (task == null) {
                throw new WebApplicationException((String.format("No open task found with task id: '%s'", documentReferentieId)),
                        Response.Status.CONFLICT
                );
            }
            assertPolicy(policyService.readTaakRechten(task, zaak).toevoegenDocument());

            final List<UUID> taakdocumenten = new ArrayList<>(taakVariabelenService.readTaakdocumenten(task));
            taakdocumenten.add(UriUtil.uuidFromURI(zaakInformatieobject.getInformatieobject()));
            taakVariabelenService.setTaakdocumenten(task, taakdocumenten);
        }
        return informatieobjectConverter.convertToREST(zaakInformatieobject);
    }

    @POST
    @Path("informatieobject/verplaats")
    public void verplaatsEnkelvoudigInformatieobject(final RESTDocumentVerplaatsGegevens documentVerplaatsGegevens) {
        final UUID enkelvoudigInformatieobjectUUID = documentVerplaatsGegevens.documentUUID;
        final EnkelvoudigInformatieObject informatieobject = drcClientService.readEnkelvoudigInformatieobject(
                enkelvoudigInformatieobjectUUID);
        final Zaak nieuweZaak = zrcClientService.readZaakByID(documentVerplaatsGegevens.nieuweZaakID);
        assertPolicy(policyService.readDocumentRechten(informatieobject, nieuweZaak).wijzigen() &&
                     policyService.readZaakRechten(nieuweZaak).wijzigen());
        final String toelichting = "Verplaatst: %s -> %s".formatted(documentVerplaatsGegevens.bron,
                nieuweZaak.getIdentificatie());
        if (documentVerplaatsGegevens.vanuitOntkoppeldeDocumenten()) {
            final OntkoppeldDocument ontkoppeldDocument = ontkoppeldeDocumentenService.read(
                    enkelvoudigInformatieobjectUUID);
            zrcClientService.koppelInformatieobject(informatieobject, nieuweZaak, toelichting);
            ontkoppeldeDocumentenService.delete(ontkoppeldDocument.getId());
        } else if (documentVerplaatsGegevens.vanuitInboxDocumenten()) {
            final InboxDocument inboxDocument = inboxDocumentenService.read(enkelvoudigInformatieobjectUUID);
            zrcClientService.koppelInformatieobject(informatieobject, nieuweZaak, toelichting);
            inboxDocumentenService.delete(inboxDocument.getId());
        } else {
            final Zaak oudeZaak = zrcClientService.readZaakByID(documentVerplaatsGegevens.bron);
            zrcClientService.verplaatsInformatieobject(informatieobject, oudeZaak, nieuweZaak);
        }
    }

    @GET
    @Path("informatieobjecttypes/{zaakTypeUuid}")
    public List<RESTInformatieobjecttype> listInformatieobjecttypes(@PathParam("zaakTypeUuid") final UUID zaakTypeID) {
        final ZaakType zaaktype = ztcClientService.readZaaktype(zaakTypeID);
        return informatieobjecttypeConverter.convert(zaaktype.getInformatieobjecttypen());
    }

    @GET
    @Path("informatieobjecttypes/zaak/{zaakUuid}")
    public List<RESTInformatieobjecttype> listInformatieobjecttypesForZaak(@PathParam("zaakUuid") final UUID zaakID) {
        final Zaak zaak = zrcClientService.readZaak(zaakID);
        final ZaakType zaaktype = ztcClientService.readZaaktype(zaak.getZaaktype());
        final List<InformatieObjectType> informatieObjectTypes = zaaktype.getInformatieobjecttypen().stream()
                .map(uri -> ztcClientService.readInformatieobjecttype(uri))
                .filter(InformatieObjectTypeUtil::isNuGeldig)
                .collect(Collectors.toList());
        return informatieobjecttypeConverter.convert(informatieObjectTypes);
    }

    /**
     * Zet een {@link RESTFileUpload} bestand in de HTTP sessie.
     *
     * @param documentReferentieId Zaak-UUID of taak-ID van gerelateerde zaak/taak.
     * @return Success response
     */
    @POST
    @Path("informatieobject/upload/{documentReferentieId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @PathParam("documentReferentieId") final String documentReferentieId,
            @MultipartForm final RESTFileUpload data
    ) {
        // note that there is no guarantee that the file will be removed from the session
        // since the user may abandon the upload process
        // this should be improved at some point
        httpSession.get().setAttribute(FILE_SESSION_ATTRIBUTE_PREFIX + documentReferentieId, data);
        return Response.ok("\"Success\"").build();
    }

    @GET
    @Path("zaakinformatieobject/{uuid}/informatieobject")
    public RESTEnkelvoudigInformatieobject readEnkelvoudigInformatieobjectByZaakInformatieobjectUUID(
            @PathParam("uuid") final UUID uuid
    ) {
        return informatieobjectConverter.convertToREST(
                drcClientService.readEnkelvoudigInformatieobject(
                        zrcClientService.readZaakinformatieobject(uuid).getInformatieobject()));
    }

    @GET
    @Path("informatieobject/{uuid}/zaakinformatieobjecten")
    public List<RESTZaakInformatieobject> listZaakInformatieobjecten(@PathParam("uuid") final UUID uuid) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieobject).lezen());
        return zrcClientService.listZaakinformatieobjecten(enkelvoudigInformatieobject).stream()
                .map(zaakInformatieobjectConverter::convert)
                .toList();
    }

    @GET
    @Path("informatieobject/{uuid}/edit")
    public Response editEnkelvoudigInformatieobjectInhoud(
            @PathParam("uuid") final UUID uuid,
            @QueryParam("zaak") final UUID zaakUUID,
            @Context final UriInfo uriInfo
    ) {
        assertPolicy(
                policyService.readDocumentRechten(drcClientService.readEnkelvoudigInformatieobject(uuid),
                        zrcClientService.readZaak(zaakUUID)).wijzigen());
        final URI redirectURI = webdavHelper.createRedirectURL(uuid, uriInfo);
        return Response.ok(redirectURI).build();
    }

    @GET
    @Path("/informatieobject/{uuid}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response readFile(@PathParam("uuid") final UUID uuid) {
        return readFile(uuid, null);
    }

    @DELETE
    @Path("/informatieobject/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response deleteEnkelvoudigInformatieObject(
            @PathParam("uuid") final UUID uuid,
            final RESTDocumentVerwijderenGegevens documentVerwijderenGegevens
    ) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        final Zaak zaak = documentVerwijderenGegevens.zaakUuid != null ?
                zrcClientService.readZaak(documentVerwijderenGegevens.zaakUuid) : null;
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieobject, zaak).verwijderen());
        zgwApiService.removeEnkelvoudigInformatieObjectFromZaak(enkelvoudigInformatieobject,
                documentVerwijderenGegevens.zaakUuid,
                documentVerwijderenGegevens.reden);

        // In geval van een ontkoppeld document
        if (documentVerwijderenGegevens.zaakUuid == null) {
            ontkoppeldeDocumentenService.delete(uuid);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/informatieobject/{uuid}/{versie}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response readFile(@PathParam("uuid") final UUID uuid, @PathParam("versie") final Integer versie) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieObject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieObject).lezen());
        try (final ByteArrayInputStream inhoud = (versie != null) ?
                drcClientService.downloadEnkelvoudigInformatieobjectVersie(uuid, versie) :
                drcClientService.downloadEnkelvoudigInformatieobject(uuid)) {
            return Response.ok(inhoud)
                    .header("Content-Disposition",
                            "attachment; filename=\"" + enkelvoudigInformatieObject.getBestandsnaam() + "\"")
                    .build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/informatieobject/{uuid}/{versie}/preview")
    public Response preview(@PathParam("uuid") final UUID uuid, @PathParam("versie") final Integer versie) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieObject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieObject).lezen());
        try (final ByteArrayInputStream inhoud = (versie != null) ?
                drcClientService.downloadEnkelvoudigInformatieobjectVersie(uuid, versie) :
                drcClientService.downloadEnkelvoudigInformatieobject(uuid)) {
            return Response.ok(inhoud)
                    .header("Content-Disposition",
                            "inline; filename=\"%s\"".formatted(enkelvoudigInformatieObject.getBestandsnaam()))
                    .header("Content-Type", enkelvoudigInformatieObject.getFormaat()).build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/download/zip")
    public Response readFilesAsZip(final List<String> uuids) {
        final List<EnkelvoudigInformatieObject> informatieobjecten = uuids.stream()
                .map(UUID::fromString)
                .map(drcClientService::readEnkelvoudigInformatieobject)
                .toList();
        informatieobjecten.forEach(
                informatieobject -> assertPolicy(policyService.readDocumentRechten(informatieobject).lezen()));
        final StreamingOutput streamingOutput = enkelvoudigInformatieObjectDownloadService.getZipStreamOutput(
                informatieobjecten);
        return Response.ok(streamingOutput).header("Content-Type", "application/zip").build();
    }

    @GET
    @Path("informatieobject/{uuid}/huidigeversie")
    public RESTEnkelvoudigInformatieObjectVersieGegevens readHuidigeVersieInformatieObject(
            @PathParam("uuid") final UUID uuid
    ) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieObject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieObject).lezen());
        return informatieobjectConverter.convertToRESTEnkelvoudigInformatieObjectVersieGegevens(
                enkelvoudigInformatieObject);
    }

    @POST
    @Path("/informatieobject/update")
    public RESTEnkelvoudigInformatieobject updateEnkelvoudigInformatieobjectWithUploadedFile(
            RESTEnkelvoudigInformatieObjectVersieGegevens enkelvoudigInformatieObjectVersieGegevens
    ) {
        final var document = drcClientService.readEnkelvoudigInformatieobject(enkelvoudigInformatieObjectVersieGegevens.uuid);
        assertPolicy(
                policyService.readDocumentRechten(
                        document,
                        zrcClientService.readZaak(enkelvoudigInformatieObjectVersieGegevens.zaakUuid)
                ).wijzigen()
        );
        final var file = (RESTFileUpload) httpSession.get().getAttribute(FILE_SESSION_ATTRIBUTE_PREFIX +
                                                                         enkelvoudigInformatieObjectVersieGegevens.zaakUuid);
        try {
            var updatedDocument = informatieobjectConverter.convert(enkelvoudigInformatieObjectVersieGegevens, file);
            return updateEnkelvoudigInformatieobject(enkelvoudigInformatieObjectVersieGegevens, document, updatedDocument);
        } finally {
            // always remove the uploaded file from the HTTP session even if exceptions are thrown
            httpSession.get().removeAttribute(FILE_SESSION_ATTRIBUTE_PREFIX + enkelvoudigInformatieObjectVersieGegevens.zaakUuid);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/informatieobject/update")
    public RESTEnkelvoudigInformatieobject updateEnkelvoudigInformatieobjectAndUploadFile(
            @Valid @MultipartForm final RESTEnkelvoudigInformatieObjectVersieGegevens enkelvoudigInformatieObjectVersieGegevens
    ) {
        final var document = drcClientService.readEnkelvoudigInformatieobject(enkelvoudigInformatieObjectVersieGegevens.uuid);
        assertPolicy(
                policyService.readDocumentRechten(
                        document,
                        zrcClientService.readZaak(enkelvoudigInformatieObjectVersieGegevens.zaakUuid)
                ).wijzigen()
        );
        var updatedDocument = informatieobjectConverter.convert(enkelvoudigInformatieObjectVersieGegevens);
        return updateEnkelvoudigInformatieobject(enkelvoudigInformatieObjectVersieGegevens, document, updatedDocument);
    }

    private RESTEnkelvoudigInformatieobject updateEnkelvoudigInformatieobject(
            RESTEnkelvoudigInformatieObjectVersieGegevens enkelvoudigInformatieObjectVersieGegevens,
            EnkelvoudigInformatieObject enkelvoudigInformatieObject,
            EnkelvoudigInformatieObjectWithLockData enkelvoudigInformatieObjectWithLockData
    ) {
        var updatedDocument = enkelvoudigInformatieObjectUpdateService.updateEnkelvoudigInformatieObjectWithLockData(
                parseUUIDFromResourceURI(enkelvoudigInformatieObject.getUrl()),
                enkelvoudigInformatieObjectWithLockData,
                enkelvoudigInformatieObjectVersieGegevens.toelichting
        );
        return informatieobjectConverter.convertToREST(convertToEnkelvoudigInformatieObject(updatedDocument));
    }

    @POST
    @Path("/informatieobject/{uuid}/lock")
    public Response lockDocument(@PathParam("uuid") final UUID uuid, @QueryParam("zaak") final UUID zaakUUID) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(uuid);
        assertPolicy(isFalse(enkelvoudigInformatieobject.getLocked()) && policyService.readDocumentRechten(
                enkelvoudigInformatieobject, zrcClientService.readZaak(zaakUUID)).vergrendelen());
        enkelvoudigInformatieObjectLockService.createLock(uuid, loggedInUserInstance.get().getId());
        // Hiervoor wordt door open zaak geen notificatie verstuurd. Dus zelf het ScreenEvent versturen!
        eventingService.send(ENKELVOUDIG_INFORMATIEOBJECT.updated(uuid));
        return Response.ok().build();
    }

    @POST
    @Path("/informatieobject/{uuid}/unlock")
    public Response unlockDocument(@PathParam("uuid") final UUID uuid, @QueryParam("zaak") final UUID zaakUUID) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(enkelvoudigInformatieobject.getLocked() && policyService.readDocumentRechten(
                enkelvoudigInformatieobject, zrcClientService.readZaak(zaakUUID)).ontgrendelen());
        enkelvoudigInformatieObjectLockService.deleteLock(uuid);
        // Hiervoor wordt door open zaak geen notificatie verstuurd. Dus zelf het ScreenEvent versturen!
        eventingService.send(ENKELVOUDIG_INFORMATIEOBJECT.updated(uuid));
        return Response.ok().build();
    }

    @GET
    @Path("informatieobject/{uuid}/historie")
    public List<RESTHistorieRegel> listHistorie(@PathParam("uuid") final UUID uuid) {
        assertPolicy(
                policyService.readDocumentRechten(drcClientService.readEnkelvoudigInformatieobject(uuid)).lezen());
        List<AuditTrailRegel> auditTrail = drcClientService.listAuditTrail(uuid);
        return historieRegelConverter.convert(auditTrail);
    }

    @POST
    @Path("/documentcreatie")
    public RESTDocumentCreatieResponse createDocument(final RESTDocumentCreatieGegevens restDocumentCreatieGegevens) {
        final Zaak zaak = zrcClientService.readZaak(restDocumentCreatieGegevens.zaakUUID);
        assertPolicy(policyService.readZaakRechten(zaak).wijzigen());

        // documents created by Smartdocuments are always of the type 'bijlage'
        // the zaaktype of the current zaak needs to be configured to be able to use this
        // informatieobjecttype
        final InformatieObjectType informatieObjectType = ztcClientService.readInformatieobjecttypen(zaak.getZaaktype()).stream()
                .filter(informatieobjecttype -> INFORMATIEOBJECTTYPE_OMSCHRIJVING_BIJLAGE.equals(informatieobjecttype.getOmschrijving()))
                .findAny()
                .orElseThrow(() -> new RuntimeException(
                        String.format("No informatieobjecttype with omschrijving '%s' found " +
                                      "for zaaktype '%s'",
                                INFORMATIEOBJECTTYPE_OMSCHRIJVING_BIJLAGE,
                                zaak.getZaaktype()))
                );

        final DocumentCreatieGegevens documentCreatieGegevens = new DocumentCreatieGegevens(
                zaak,
                restDocumentCreatieGegevens.taskId,
                informatieObjectType
        );
        final DocumentCreatieResponse documentCreatieResponse = documentCreatieService.creeerDocumentAttendedSD(
                documentCreatieGegevens);
        return new RESTDocumentCreatieResponse(documentCreatieResponse.getRedirectUrl(),
                documentCreatieResponse.getMessage());
    }

    @GET
    @Path("informatieobject/{informatieObjectUuid}/zaakidentificaties")
    public List<String> listZaakIdentificatiesForInformatieobject(
            @PathParam("informatieObjectUuid") UUID informatieobjectUuid
    ) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(
                informatieobjectUuid);
        assertPolicy(policyService.readDocumentRechten(enkelvoudigInformatieobject).lezen());
        List<ZaakInformatieobject> zaakInformatieobjects = zrcClientService.listZaakinformatieobjecten(
                enkelvoudigInformatieobject);
        return zaakInformatieobjects.stream()
                .map(zaakInformatieobject -> zrcClientService.readZaak(zaakInformatieobject.getZaak())
                        .getIdentificatie()).toList();
    }

    @POST
    @Path("/informatieobject/{uuid}/onderteken")
    public Response ondertekenInformatieObject(
            @PathParam("uuid") final UUID uuid,
            @QueryParam("zaak") final UUID zaakUUID
    ) {
        final EnkelvoudigInformatieObject enkelvoudigInformatieobject = drcClientService.readEnkelvoudigInformatieobject(
                uuid);
        assertPolicy(enkelvoudigInformatieobject.getOndertekening() == null &&
                     policyService.readDocumentRechten(enkelvoudigInformatieobject,
                             zrcClientService.readZaak(zaakUUID)).ondertekenen());
        enkelvoudigInformatieObjectUpdateService.ondertekenEnkelvoudigInformatieObject(uuid);

        // Hiervoor wordt door open zaak geen notificatie verstuurd. Dus zelf het ScreenEvent versturen!
        eventingService.send(ENKELVOUDIG_INFORMATIEOBJECT.updated(enkelvoudigInformatieobject));

        return Response.ok().build();
    }

    @POST
    @Path("/informatieobject/{uuid}/convert")
    public Response convertInformatieObjectToPDF(
            @PathParam("uuid") final UUID enkelvoudigInformatieobjectUUID,
            @QueryParam("zaak") final UUID zaakUUID
    ) throws IOException {
        final EnkelvoudigInformatieObject document = drcClientService.readEnkelvoudigInformatieobject(enkelvoudigInformatieobjectUUID);
        assertPolicy(policyService.readDocumentRechten(document, zrcClientService.readZaak(zaakUUID)).wijzigen());
        try (final ByteArrayInputStream documentInputStream = drcClientService.downloadEnkelvoudigInformatieobject(
                enkelvoudigInformatieobjectUUID);
             final ByteArrayInputStream pdfInputStream = officeConverterClientService.convertToPDF(documentInputStream, document
                     .getBestandsnaam())) {
            final EnkelvoudigInformatieObjectWithLockData pdf = new EnkelvoudigInformatieObjectWithLockData();
            final byte[] inhoud = pdfInputStream.readAllBytes();
            pdf.setInhoud(convertByteArrayToBase64String(inhoud));
            pdf.setFormaat(MEDIA_TYPE_PDF);
            pdf.setBestandsnaam(StringUtils.substringBeforeLast(document.getBestandsnaam(), ".") + ".pdf");
            pdf.setBestandsomvang(inhoud.length);
            enkelvoudigInformatieObjectUpdateService.updateEnkelvoudigInformatieObjectWithLockData(
                    parseUUIDFromResourceURI(document.getUrl()),
                    pdf,
                    TOELICHTING_PDF
            );
        }
        return Response.ok().build();
    }

    private boolean isVerzendenToegestaan(final EnkelvoudigInformatieObject informatieobject) {
        var vertrouwelijkheidaanduiding = informatieobject.getVertrouwelijkheidaanduiding();
        return informatieobject.getStatus() == EnkelvoudigInformatieObject.StatusEnum.DEFINITIEF &&
               vertrouwelijkheidaanduiding != EnkelvoudigInformatieObject.VertrouwelijkheidaanduidingEnum.CONFIDENTIEEL &&
               vertrouwelijkheidaanduiding != EnkelvoudigInformatieObject.VertrouwelijkheidaanduidingEnum.GEHEIM &&
               vertrouwelijkheidaanduiding != EnkelvoudigInformatieObject.VertrouwelijkheidaanduidingEnum.ZEER_GEHEIM &&
               informatieobject.getOntvangstdatum() == null &&
               MEDIA_TYPE_PDF.equals(informatieobject.getFormaat());
    }

    private List<RESTEnkelvoudigInformatieobject> listEnkelvoudigInformatieobjectenVoorZaak(final Zaak zaak) {
        return informatieobjectConverter.convertToREST(zrcClientService.listZaakinformatieobjecten(zaak));
    }

    private List<RESTGekoppeldeZaakEnkelvoudigInformatieObject> listGekoppeldeZaakEnkelvoudigInformatieobjectenVoorZaak(
            final URI zaakURI,
            final RelatieType relatieType
    ) {
        final Zaak zaak = zrcClientService.readZaak(zaakURI);
        return zrcClientService.listZaakinformatieobjecten(zaak).stream()
                .map(zaakInformatieobject -> informatieobjectConverter.convertToREST(
                        zaakInformatieobject,
                        relatieType,
                        zaak
                )).toList();
    }

    private List<RESTGekoppeldeZaakEnkelvoudigInformatieObject> listGekoppeldeZaakInformatieObjectenVoorZaak(
            final Zaak zaak
    ) {
        final List<RESTGekoppeldeZaakEnkelvoudigInformatieObject> enkelvoudigInformatieobjectList = new ArrayList<>();
        zaak.getDeelzaken().forEach(deelzaak -> enkelvoudigInformatieobjectList.addAll(
                listGekoppeldeZaakEnkelvoudigInformatieobjectenVoorZaak(deelzaak, RelatieType.DEELZAAK)));
        if (zaak.getHoofdzaak() != null) {
            enkelvoudigInformatieobjectList.addAll(
                    listGekoppeldeZaakEnkelvoudigInformatieobjectenVoorZaak(zaak.getHoofdzaak(),
                            RelatieType.HOOFDZAAK));
        }
        zaak.getRelevanteAndereZaken().forEach(relevanteAndereZaak -> enkelvoudigInformatieobjectList.addAll(
                listGekoppeldeZaakEnkelvoudigInformatieobjectenVoorZaak(
                        relevanteAndereZaak.getUrl(),
                        gerelateerdeZaakConverter.convertToRelatieType(relevanteAndereZaak.getAardRelatie())
                )
        ));
        return enkelvoudigInformatieobjectList;
    }
}
