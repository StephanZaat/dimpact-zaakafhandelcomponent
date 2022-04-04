/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.documenten;

import static net.atos.zac.util.ValidationUtil.valideerObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import net.atos.client.zgw.drc.model.EnkelvoudigInformatieobject;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.zac.authentication.IngelogdeMedewerker;
import net.atos.zac.authentication.Medewerker;
import net.atos.zac.documenten.model.OntkoppeldDocument;
import net.atos.zac.shared.model.ListParameters;
import net.atos.zac.shared.model.SortDirection;
import net.atos.zac.util.UriUtil;

@ApplicationScoped
@Transactional
public class OntkoppeldeDocumentenService {

    @PersistenceContext(unitName = "ZaakafhandelcomponentPU")
    private EntityManager entityManager;

    @Inject
    @IngelogdeMedewerker
    private Instance<Medewerker> ingelogdeMedewerker;

    public OntkoppeldDocument create(final OntkoppeldDocument document) {
        valideerObject(document);
        entityManager.persist(document);
        return document;
    }

    public OntkoppeldDocument create(final EnkelvoudigInformatieobject informatieobject, final Zaak zaak, final String reden) {
        final OntkoppeldDocument ontkoppeldDocument = new OntkoppeldDocument();
        ontkoppeldDocument.setDocumentID(informatieobject.getIdentificatie());
        ontkoppeldDocument.setDocumentUUID(UriUtil.uuidFromURI(informatieobject.getUrl()));
        ontkoppeldDocument.setCreatiedatum(informatieobject.getCreatiedatum().atStartOfDay(ZoneId.systemDefault()));
        ontkoppeldDocument.setTitel(informatieobject.getTitel());
        ontkoppeldDocument.setBestandsnaam(informatieobject.getBestandsnaam());
        ontkoppeldDocument.setOntkoppeldOp(ZonedDateTime.now());
        ontkoppeldDocument.setOntkoppeldDoor(ingelogdeMedewerker.get().getGebruikersnaam());
        ontkoppeldDocument.setZaakID(zaak.getIdentificatie());
        ontkoppeldDocument.setReden(reden);
        return create(ontkoppeldDocument);
    }

    public List<OntkoppeldDocument> list(final ListParameters listParameters) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<OntkoppeldDocument> query = builder.createQuery(OntkoppeldDocument.class);
        final Root<OntkoppeldDocument> root = query.from(OntkoppeldDocument.class);
        if (listParameters.getSorting() != null) {
            if (listParameters.getSorting().getDirection() == SortDirection.ASCENDING) {
                query.orderBy(builder.asc(root.get(listParameters.getSorting().getField())));
            } else {
                query.orderBy(builder.desc(root.get(listParameters.getSorting().getField())));
            }
        }
        final TypedQuery<OntkoppeldDocument> emQuery = entityManager.createQuery(query);
        if (listParameters.getPaging() != null) {
            emQuery.setFirstResult(listParameters.getPaging().getFirstResult());
            emQuery.setMaxResults(listParameters.getPaging().getMaxResults());
        }
        return emQuery.getResultList();
    }

    public int count() {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = builder.createQuery(Long.class);
        final Root<OntkoppeldDocument> root = query.from(OntkoppeldDocument.class);
        query.select(builder.count(root));
        final Long result = entityManager.createQuery(query).getSingleResult();
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    public OntkoppeldDocument update(final OntkoppeldDocument ontkoppeldDocument) {
        valideerObject(ontkoppeldDocument);
        return entityManager.merge(ontkoppeldDocument);
    }

    public void delete(final Long id) {
        final OntkoppeldDocument ontkoppeldDocument = entityManager.find(OntkoppeldDocument.class, id);
        if (ontkoppeldDocument != null) {
            entityManager.remove(ontkoppeldDocument);
        }
    }
}
