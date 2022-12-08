/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {TextIcon} from '../../../shared/edit/text-icon';
import {Conditionals} from '../../../shared/edit/conditional-fn';
import {IndicatiesLayout} from '../../../shared/indicaties/indicaties.component';

@Component({template: ''})
export abstract class ZoekObjectComponent {
    readonly indicatiesLayout = IndicatiesLayout;
    abstract sideNav: MatSidenav;
    viewIcon = new TextIcon(Conditionals.always, 'visibility', 'visibility_icon', '', 'pointer');

    protected constructor() {
    }

    protected _open(): void {
        this.sideNav.close();
    }

}
