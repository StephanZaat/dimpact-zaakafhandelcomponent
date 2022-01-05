/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component, Input} from '@angular/core';
import {EditComponent} from '../edit.component';
import {MaterialFormBuilderService} from '../../material-form-builder/material-form-builder.service';
import {AutocompleteFormField} from '../../material-form-builder/form-components/autocomplete/autocomplete-form-field';
import {UtilService} from '../../../core/service/util.service';

@Component({
    selector: 'zac-edit-autocomplete',
    templateUrl: './edit-autocomplete.component.html',
    styleUrls: ['../../static-text/static-text.component.less', '../edit.component.less', './edit-autocomplete.component.less']
})
export class EditAutocompleteComponent extends EditComponent {

    @Input() formField: AutocompleteFormField;

    constructor(mfbService: MaterialFormBuilderService, utilService: UtilService) {
        super(mfbService, utilService);
    }

    init(formField: AutocompleteFormField): void {
        this.value = formField.formControl.value ? formField.formControl.value[formField.optionLabel] : formField.formControl.value;

        this.subscription = formField.formControl.valueChanges.subscribe(() => {
            this.dirty = true;
        });
    }

    edit(editing: boolean): void {
        super.edit(editing);
        this.formItem.data.formControl.setValue(null);
        this.dirty = false;
    }

}
