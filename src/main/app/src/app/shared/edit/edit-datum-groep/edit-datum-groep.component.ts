/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EditComponent} from '../edit.component';
import {MaterialFormBuilderService} from '../../material-form-builder/material-form-builder.service';
import {DateFormField} from '../../material-form-builder/form-components/date/date-form-field';
import {UtilService} from '../../../core/service/util.service';
import {TextIcon} from '../text-icon';
import {FormControl, Validators} from '@angular/forms';
import {InputFormField} from '../../material-form-builder/form-components/input/input-form-field';
import * as moment from 'moment/moment';
import {Moment} from 'moment/moment';
import {DialogComponent} from '../../dialog/dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {DialogData} from '../../dialog/dialog-data';
import {ZakenService} from '../../../zaken/zaken.service';
import {InputFormFieldBuilder} from '../../material-form-builder/form-components/input/input-form-field-builder';
import {TranslateService} from '@ngx-translate/core';
import {Observable, of, Subscription} from 'rxjs';
import {CheckboxFormFieldBuilder} from '../../material-form-builder/form-components/checkbox/checkbox-form-field-builder';
import {CheckboxFormField} from '../../material-form-builder/form-components/checkbox/checkbox-form-field';
import {HiddenFormFieldBuilder} from '../../material-form-builder/form-components/hidden/hidden-form-field-builder';
import {HiddenFormField} from '../../material-form-builder/form-components/hidden/hidden-form-field';
import {AbstractFormField} from '../../material-form-builder/model/abstract-form-field';

@Component({
    selector: 'zac-edit-datum-groep',
    templateUrl: './edit-datum-groep.component.html',
    styleUrls: ['../../static-text/static-text.component.less', '../edit.component.less', './edit-datum-groep.component.less']
})
export class EditDatumGroepComponent extends EditComponent implements OnInit {

    @Input() formField: DateFormField;
    @Input() startDatumField: DateFormField;
    @Input() streefDatumField: DateFormField;
    @Input() fataleDatumField: DateFormField;
    @Input() streefDatumIcon: TextIcon;
    @Input() fataleDatumIcon: TextIcon;
    @Input() reasonField: InputFormField;
    @Input() opgeschort: boolean;
    @Input() opschortReden: string;
    @Input() opschortDuur: number;
    @Input() opschortDatumTijd: string;
    @Output() doOpschorting: EventEmitter<any> = new EventEmitter<any>();
    @Input() verlengReden: string;
    @Input() verlengDuur: string;
    @Output() doVerlenging: EventEmitter<any> = new EventEmitter<any>();

    showStreefDatumIcon: boolean;
    showFataleDatumIcon: boolean;
    showStreefError: boolean;
    showFataleError: boolean;

    startDatum: string;
    streefDatum: string;
    fataleDatum: string;

    duurField: InputFormField;
    werkelijkeOpschortDuur: number;

    editFormFieldIcons: Map<string, TextIcon> = new Map<string, TextIcon>();

    constructor(mfbService: MaterialFormBuilderService,
                utilService: UtilService,
                private zakenService: ZakenService,
                private dialog: MatDialog,
                private translate: TranslateService) {
        super(mfbService, utilService);
    }

    ngOnInit(): void {
        this.updateGroep();
    }

    updateGroep(): void {
        this.startDatum = this.startDatumField.formControl.value;
        this.streefDatum = this.streefDatumField.formControl.value;
        this.fataleDatum = this.fataleDatumField.formControl.value;
        this.showStreefDatumIcon = this.streefDatumIcon?.showIcon(new FormControl(this.streefDatumField.formControl.value));
        this.showFataleDatumIcon = this.fataleDatumIcon?.showIcon(new FormControl(this.fataleDatumField.formControl.value));
    }

    init(formField: DateFormField): void {
    }

    save(): void {
        this.validate();
        if (!this.showStreefError && !this.showFataleError && this.startDatumField.formControl.valid &&
            this.streefDatumField.formControl.valid && this.fataleDatumField.formControl.valid) {
            this.onSave.emit({
                startdatum: this.startDatumField.formControl.value,
                einddatumGepland: this.streefDatumField.formControl.value,
                uiterlijkeEinddatumAfdoening: this.fataleDatumField.formControl.value,
                reden: this.reasonField?.formControl.value
            });
            this.updateGroep();
            this.editing = false;
        }
    }

    validate(): void {
        const start: Moment = moment(this.startDatumField.formControl.value);
        const fatale: Moment = moment(this.fataleDatumField.formControl.value);
        if (this.streefDatumField.formControl.value) {
            const streef: Moment = moment(this.streefDatumField.formControl.value);
            this.showStreefError = streef.isBefore(start);
            this.showFataleError = fatale.isBefore(streef);
        } else {
            this.showFataleError = fatale.isBefore(start);
        }
        this.dirty = true;
    }

    hasError(): boolean {
        return this.showStreefError || this.showFataleError ||
            this.startDatumField.formControl.invalid ||
            this.streefDatumField.formControl.invalid ||
            this.fataleDatumField.formControl.invalid;
    }

    edit(editing: boolean): void {
        if (!this.readonly && !this.utilService.hasEditOverlay()) {
            this.editing = editing;
            this.startDatumField.formControl.markAsUntouched();
            this.streefDatumField.formControl.markAsUntouched();
            this.startDatumField.formControl.markAsUntouched();
            this.reasonField.formControl.setValue(null);
            this.dirty = false;
        }
    }

    cancel(): void {
        this.startDatumField.formControl.setValue(this.startDatum);
        this.streefDatumField.formControl.setValue(this.streefDatum);
        this.fataleDatumField.formControl.setValue(this.fataleDatum);
        this.showStreefError = false;
        this.showFataleError = false;
        this.editing = false;
    }

    private maakDuurField(label: string): InputFormField {
        this.duurField = new InputFormFieldBuilder()
        .id('duurDagen')
        .label(label)
        .validators(Validators.required, Validators.min(1))
        .build();
        return this.duurField;
    }

    private maakHiddenField(field: AbstractFormField): HiddenFormField {
        return new HiddenFormFieldBuilder()
        .id(field.id)
        .label(field.label)
        .value(field.formControl.value)
        .build();
    }

    private maakRedenField(reden: string): InputFormField {
        return new InputFormFieldBuilder()
        .id('reden')
        .label('reden')
        .value(reden)
        .validators(Validators.required)
        .build();
    }

    private maakTakenField(label: string): CheckboxFormField {
        return new CheckboxFormFieldBuilder()
        .id('takenVerlengen')
        .label(label)
        .build();
    }

    opschorten() {
        const heeftStreefDatum: boolean = this.streefDatumField.formControl.value;
        const dialogData = new DialogData([
                this.maakDuurField('opschortduur'),
                heeftStreefDatum ? this.streefDatumField : this.maakHiddenField(this.streefDatumField),
                this.fataleDatumField,
                this.maakRedenField(this.opschortReden)
            ],
            (results: any[]) => this.saveOpschorting(results),
            this.translate.instant('msg.zaak.opschorten'));

        const vorigeStreefDatum: Moment = heeftStreefDatum ? moment(this.streefDatumField.formControl.value) : null;
        const vorigeFataleDatum: Moment = moment(this.fataleDatumField.formControl.value);
        const subscriptions = this.subscribe(vorigeStreefDatum, vorigeFataleDatum);

        this.dialog.open(DialogComponent, {
            data: dialogData
        }).afterClosed().subscribe(result => {
            this.unsubscribe(subscriptions);
            if (!result) {
                this.resetDatums(vorigeStreefDatum, vorigeFataleDatum);
            }
            this.updateGroep();
        });
    }

    saveOpschorting(results: any[]): Observable<void> {
        this.doOpschorting.emit(results);
        return of(null);
    }

    hervatten() {
        this.werkelijkeOpschortDuur = moment().diff(moment(this.opschortDatumTijd), 'days');
        const dialogData = new DialogData([
                this.maakRedenField(this.opschortReden)
            ],
            (results: any[]) => this.saveHervatting(results),
            this.translate.instant('msg.zaak.hervatten', {duur: this.werkelijkeOpschortDuur, verwachteDuur: this.opschortDuur}));

        this.dialog.open(DialogComponent, {
            data: dialogData
        });
    }

    saveHervatting(results: any[]): Observable<void> {
        const heeftStreefDatum: boolean = this.streefDatumField.formControl.value;
        const duurVerschil: number = this.werkelijkeOpschortDuur - this.opschortDuur;
        if (heeftStreefDatum) {
            this.streefDatumField.formControl.setValue(moment(this.streefDatumField.formControl.value).add(duurVerschil, 'days'));
        }
        this.fataleDatumField.formControl.setValue(moment(this.fataleDatumField.formControl.value).add(duurVerschil, 'days'));
        this.updateGroep();
        results['duurDagen'] = this.werkelijkeOpschortDuur;
        results['einddatumGepland'] = this.streefDatumField.formControl.value;
        results['uiterlijkeEinddatumAfdoening'] = this.fataleDatumField.formControl.value;
        return this.saveOpschorting(results);
    }

    verlengen() {
        const heeftStreefDatum: boolean = this.streefDatumField.formControl.value;
        const dialogData = new DialogData([
                this.maakDuurField('verlengduur'),
                heeftStreefDatum ? this.streefDatumField : this.maakHiddenField(this.streefDatumField),
                this.fataleDatumField,
                this.maakRedenField(this.verlengReden),
                this.maakTakenField('taken.verlengen')
            ],
            (results: any[]) => this.saveVerlenging(results),
            this.translate.instant(this.verlengDuur ? 'msg.zaak.verlengen.meer' : 'msg.zaak.verlengen', {eerdereDuur: this.verlengDuur}));

        const vorigeStreefDatum: Moment = heeftStreefDatum ? moment(this.streefDatumField.formControl.value) : null;
        const vorigeFataleDatum: Moment = moment(this.fataleDatumField.formControl.value);
        const subscriptions = this.subscribe(vorigeStreefDatum, vorigeFataleDatum);

        this.dialog.open(DialogComponent, {
            data: dialogData
        }).afterClosed().subscribe(result => {
            this.unsubscribe(subscriptions);
            if (!result) {
                this.resetDatums(vorigeStreefDatum, vorigeFataleDatum);
            }
            this.updateGroep();
        });
    }

    saveVerlenging(results: any[]): Observable<void> {
        this.doVerlenging.emit(results);
        return of(null);
    }

    private subscribe(vorigeStreefDatum: Moment, vorigeFataleDatum: Moment): Subscription[] {
        const subscriptions: Subscription[] = [];
        subscriptions.push(this.duurField.formControl.valueChanges.subscribe(value => {
            this.duurChanged(value, vorigeStreefDatum, vorigeFataleDatum);
        }));
        subscriptions.push(this.streefDatumField.formControl.valueChanges.subscribe(value => {
            this.streefChanged(value, vorigeStreefDatum, vorigeFataleDatum);
        }));
        subscriptions.push(this.fataleDatumField.formControl.valueChanges.subscribe(value => {
            this.fataleChanged(value, vorigeStreefDatum, vorigeFataleDatum);
        }));
        return subscriptions;
    }

    private unsubscribe(subscriptions: Subscription[]): void {
        for (const subscription of subscriptions) {
            subscription.unsubscribe();
        }
    }

    duurChanged(value: string, vorigeStreefDatum: Moment, vorigeFataleDatum: Moment) {
        let duur: number = Number(value);
        if (value == null || isNaN(duur)) {
            duur = 0;
        }
        this.updateDatums(duur, vorigeStreefDatum, vorigeFataleDatum);
    }

    streefChanged(value: any, vorigeStreefDatum: Moment, vorigeFataleDatum: Moment) {
        if (value != null) {
            this.updateDatums(moment(value).diff(vorigeStreefDatum, 'days'), vorigeStreefDatum, vorigeFataleDatum);
        }
    }

    fataleChanged(value: any, vorigeStreefDatum: Moment, vorigeFataleDatum: Moment) {
        if (value != null) {
            this.updateDatums(moment(value).diff(vorigeFataleDatum, 'days'), vorigeStreefDatum, vorigeFataleDatum);
        }
    }

    private updateDatums(duur: number, vorigeStreefDatum: Moment, vorigeFataleDatum: Moment) {
        if (0 < duur) {
            this.duurField.formControl.setValue(duur, {emitEvent: false});
            if (vorigeStreefDatum != null) {
                this.streefDatumField.formControl.setValue(moment(vorigeStreefDatum).add(duur, 'days'), {emitEvent: false});
            }
            this.fataleDatumField.formControl.setValue(moment(vorigeFataleDatum).add(duur, 'days'), {emitEvent: false});
        } else {
            this.resetDatums(vorigeStreefDatum, vorigeFataleDatum);
        }
    }

    private resetDatums(vorigeStreefDatum: Moment, vorigeFataleDatum: Moment) {
        this.duurField.formControl.setValue(null, {emitEvent: false});
        this.streefDatumField.formControl.setValue(vorigeStreefDatum, {emitEvent: false});
        this.fataleDatumField.formControl.setValue(vorigeFataleDatum, {emitEvent: false});
    }
}
