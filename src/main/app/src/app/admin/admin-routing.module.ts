/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ParametersComponent} from './parameters/parameters.component';
import {ParameterEditComponent} from './parameter-edit/parameter-edit.component';
import {ZaakafhandelParametersResolver} from './zaakafhandel-parameters-resolver.service';
import {GroepSignaleringenComponent} from './groep-signaleringen/groep-signaleringen.component';
import {ReferentieTabellenComponent} from './referentie-tabellen/referentie-tabellen.component';
import {ReferentieTabelResolver} from './referentie-tabel-resolver.service';
import {ReferentieTabelComponent} from './referentie-tabel/referentie-tabel.component';
import {InrichtingscheckComponent} from './inrichtingscheck/inrichtingscheck.component';

const routes: Routes = [
    {
        path: 'admin', children: [
            {path: '', redirectTo: 'check', pathMatch: 'full'},
            {path: 'groepen', component: GroepSignaleringenComponent},
            {path: 'parameters', component: ParametersComponent},
            {path: 'parameters/:uuid', component: ParameterEditComponent, resolve: {parameters: ZaakafhandelParametersResolver}},
            {path: 'referentietabellen', component: ReferentieTabellenComponent},
            {path: 'referentietabellen/:id', component: ReferentieTabelComponent, resolve: {tabel: ReferentieTabelResolver}},
            {path: 'check', component: InrichtingscheckComponent}
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminRoutingModule {
}
