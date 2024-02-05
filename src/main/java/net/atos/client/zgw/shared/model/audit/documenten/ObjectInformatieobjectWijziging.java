/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.client.zgw.shared.model.audit.documenten;

import net.atos.client.zgw.drc.model.generated.ObjectInformatieObject;
import net.atos.client.zgw.shared.model.ObjectType;
import net.atos.client.zgw.shared.model.audit.AuditWijziging;

public class ObjectInformatieobjectWijziging extends AuditWijziging<ObjectInformatieObject> {

    @Override
    public ObjectType getObjectType() {
        return ObjectType.OBJECT_INFORMATIEOBJECT;
    }
}
