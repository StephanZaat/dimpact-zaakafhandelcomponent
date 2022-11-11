/*
 * SPDX-FileCopyrightText: 2021-2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'zac-klant-filter-dialog',
    templateUrl: 'klant-filter.dialog.html',
    styleUrls: ['./klant-filter.dialog.less']
})
export class KlantFilterDialog {
    constructor(public dialogRef: MatDialogRef<KlantFilterDialog>) {}
}
