/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.planitems;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.idm.api.Group;

import net.atos.zac.app.planitems.converter.RESTPlanItemConverter;
import net.atos.zac.app.planitems.model.RESTPlanItem;
import net.atos.zac.flowable.FlowableService;

/**
 *
 */
@Path("planitems")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlanItemsRESTService {

    @Inject
    private FlowableService flowableService;

    @Inject
    private RESTPlanItemConverter planItemConverter;

    @GET
    @Path("zaak/{uuid}")
    public List<RESTPlanItem> getPlanItemsForZaak(@PathParam("uuid") final UUID zaakUUID) {
        final List<PlanItemInstance> planItems = flowableService.listPlanItemsForZaak(zaakUUID);
        return planItemConverter.convertPlanItems(planItems);
    }

    @GET
    @Path("{id}")
    public RESTPlanItem getPlanItem(@PathParam("id") final String planItemId) {
        final PlanItemInstance planItem = flowableService.findPlanItem(planItemId);
        final Group group = flowableService.findGroupForPlanItem(planItemId);
        return planItemConverter.convertPlanItemMetGroep(planItem, group);
    }

    @PUT
    @Path("do/{id}")
    public RESTPlanItem doPlanItem(final RESTPlanItem restPlanItem) {
        if (restPlanItem.groep != null) {
            flowableService.startHumanTaskPlanItem(restPlanItem.id, restPlanItem.groep.id);
        } else {
            flowableService.startPlanItem(restPlanItem.id);
        }
        return restPlanItem;
    }
}
