/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.klanten;

import static net.atos.zac.app.klanten.converter.RESTPersoonConverter.FIELDS_PERSOON;
import static net.atos.zac.app.klanten.converter.RESTPersoonConverter.ONBEKEND;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.atos.client.brp.BRPClientService;
import net.atos.client.brp.model.IngeschrevenPersoonHal;
import net.atos.client.brp.model.IngeschrevenPersoonHalCollectie;
import net.atos.client.brp.model.ListPersonenParameters;
import net.atos.client.klanten.KlantenClientService;
import net.atos.client.klanten.model.generated.Klant;
import net.atos.client.kvk.KVKClientService;
import net.atos.client.kvk.model.KVKZoekenParameters;
import net.atos.client.kvk.zoeken.model.Resultaat;
import net.atos.client.kvk.zoeken.model.ResultaatItem;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.AardVanRol;
import net.atos.client.zgw.ztc.model.Roltype;
import net.atos.zac.app.klanten.converter.RESTBedrijfConverter;
import net.atos.zac.app.klanten.converter.RESTPersoonConverter;
import net.atos.zac.app.klanten.converter.RESTRoltypeConverter;
import net.atos.zac.app.klanten.model.bedrijven.RESTBedrijf;
import net.atos.zac.app.klanten.model.bedrijven.RESTListBedrijvenParameters;
import net.atos.zac.app.klanten.model.klant.RESTRoltype;
import net.atos.zac.app.klanten.model.personen.RESTListPersonenParameters;
import net.atos.zac.app.klanten.model.personen.RESTPersoon;
import net.atos.zac.app.shared.RESTResultaat;

@Path("klanten")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class KlantenRESTService {

    public static final Set<AardVanRol> betrokkenen;

    private static final Logger LOG = Logger.getLogger(KlantenRESTService.class.getName());

    private static final RESTPersoon ONBEKEND_PERSOON = new RESTPersoon(ONBEKEND, ONBEKEND, ONBEKEND);

    static {
        betrokkenen = EnumSet.allOf(AardVanRol.class);
        betrokkenen.remove(AardVanRol.INITIATOR);
        betrokkenen.remove(AardVanRol.BEHANDELAAR);
    }

    @Inject
    private BRPClientService brpClientService;

    @Inject
    private KVKClientService kvkClientService;

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private RESTPersoonConverter persoonConverter;

    @Inject
    private RESTBedrijfConverter bedrijfConverter;

    @Inject
    private RESTRoltypeConverter roltypeConverter;

    @Inject
    private KlantenClientService klantenClientService;

    @GET
    @Path("persoon/{bsn}")
    public RESTPersoon readPersoon(@PathParam("bsn") final String bsn) throws ExecutionException, InterruptedException {
        return brpClientService.findPersoonAsync(bsn, FIELDS_PERSOON)
                .thenCombine(klantenClientService.findKlantAsync(bsn), (persoon, klant) -> convert(persoon, klant))
                .toCompletableFuture()
                .get();
    }

    private RESTPersoon convert(final Optional<IngeschrevenPersoonHal> persoon, final Optional<Klant> klant) {
        return persoon
                .map(persoonConverter::convertToPersoon)
                .map(restPersoon -> addKlantGegevens(restPersoon, klant))
                .orElse(ONBEKEND_PERSOON);
    }

    private RESTPersoon addKlantGegevens(final RESTPersoon restPersoon, final Optional<Klant> klantOptional) {
        klantOptional.ifPresent(klant -> {
            restPersoon.telefoonnummer = klant.getTelefoonnummer();
            restPersoon.emailadres = klant.getEmailadres();
        });
        return restPersoon;
    }

    @GET
    @Path("vestiging/{vestigingsnummer}")
    public RESTBedrijf readVestiging(@PathParam("vestigingsnummer") final String vestigingsnummer) {
        return kvkClientService.findVestiging(vestigingsnummer)
                .map(bedrijfConverter::convert)
                .orElse(new RESTBedrijf());
    }

    @GET
    @Path("rechtspersoon/{rsin}")
    public RESTBedrijf readRechtspersoon(@PathParam("rsin") final String rsin) {
        return kvkClientService.findRechtspersoon(rsin)
                .map(bedrijfConverter::convert)
                .orElse(new RESTBedrijf());
    }

    @PUT
    @Path("personen")
    public RESTResultaat<RESTPersoon> listPersonen(final RESTListPersonenParameters restListPersonenParameters) {
        try {
            final ListPersonenParameters listPersonenParameters = persoonConverter.convert(restListPersonenParameters);
            final IngeschrevenPersoonHalCollectie ingeschrevenPersoonHalCollectie = brpClientService.listPersonen(
                    listPersonenParameters);
            return new RESTResultaat<>(
                    persoonConverter.convert(ingeschrevenPersoonHalCollectie.getEmbedded().getIngeschrevenpersonen()));
        } catch (final RuntimeException e) {
            LOG.severe(() -> String.format("Error while calling listPersonen: %s", e.getMessage()));
            return new RESTResultaat<>(e.getMessage());
        }
    }

    @PUT
    @Path("bedrijven")
    public RESTResultaat<RESTBedrijf> listBedrijven(final RESTListBedrijvenParameters restParameters) {
        try {
            final KVKZoekenParameters zoekenParameters = bedrijfConverter.convert(restParameters);
            final Resultaat resultaat = kvkClientService.find(zoekenParameters);
            if (resultaat.getResultaten() == null) {
                return new RESTResultaat<>(Collections.emptyList());
            }
            return new RESTResultaat<>(resultaat.getResultaten().stream()
                                               .filter(KlantenRESTService::isKoppelbaar)
                                               .map(bedrijfConverter::convert)
                                               .toList());
        } catch (final RuntimeException e) {
            LOG.severe(() -> String.format("Error while calling listBedrijven: %s", e.getMessage()));
            return new RESTResultaat<>(e.getMessage());
        }
    }

    private static boolean isKoppelbaar(final ResultaatItem item) {
        return item.getVestigingsnummer() != null || item.getRsin() != null;
    }

    @GET
    @Path("roltype/{zaaktypeUuid}/betrokkene")
    public List<RESTRoltype> listBetrokkeneRoltypen(@PathParam("zaaktypeUuid") final UUID zaaktype) {
        return roltypeConverter.convert(
                ztcClientService.listRoltypen(ztcClientService.readZaaktype(zaaktype).getUrl()).stream()
                        .filter(roltype -> betrokkenen.contains(roltype.getOmschrijvingGeneriek()))
                        .sorted(Comparator.comparing(Roltype::getOmschrijving)));
    }
}
