/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormComponent} from '../../model/form-component';
import {TranslateService} from '@ngx-translate/core';
import {IdentityService} from '../../../../identity/identity.service';
import {FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {Observable, Subscription} from 'rxjs';
import {Group} from '../../../../identity/model/group';
import {User} from '../../../../identity/model/user';
import {MedewerkerGroepFormField} from './medewerker-groep-form-field';
import {AutocompleteValidators} from '../autocomplete/autocomplete-validators';
import {map, startWith} from 'rxjs/operators';

@Component({
    templateUrl: './medewerker-groep.component.html',
    styleUrls: ['./medewerker-groep.component.less']
})
export class MedewerkerGroepComponent extends FormComponent implements OnInit, OnDestroy {
    data: MedewerkerGroepFormField;
    groepen: Group[];
    filteredGroepen: Observable<Group[]>;
    medewerkers: User[];
    filteredMedewerkers: Observable<User[]>;
    inGroep: boolean = true;
    subscriptions$: Subscription[] = [];

    constructor(public translate: TranslateService, public identityService: IdentityService) {
        super();
    }

    ngOnInit(): void {
        this.initGroepen();


        this.subscriptions$.push(
            this.data.groep.valueChanges.subscribe((value) => {
                if (this.data.groep.valid && this.data.groep.dirty) {
                    this.data.medewerker.setValue(null);
                    this.getMedewerkers();
                }
            })
        );

        this.getMedewerkers();
    }

    ngOnDestroy() {
        this.subscriptions$.forEach(s => s.unsubscribe());
    }

    initGroepen(): void {
        this.identityService.listGroups().subscribe(groepen => {
            this.groepen = groepen;
            const validators:ValidatorFn[] = [];
            validators.push(AutocompleteValidators.optionInList(groepen));
            if(this.data.groep.hasValidator(Validators.required)){
                validators.push(Validators.required);
            }
            this.data.groep.setValidators(validators);
            this.data.groep.updateValueAndValidity();

            this.filteredGroepen = this.data.groep.valueChanges.pipe(
                startWith(''),
                map(groep => (groep ? this._filterGroepen(groep) : this.groepen.slice()))
            );
        });
    }

    inGroepChanged($event: MouseEvent) {
        $event.stopPropagation();
        this.inGroep = !this.inGroep;
        this.getMedewerkers();
    }

    private getMedewerkers() {
        this.medewerkers = [];
        let observable: Observable<User[]>;
        if (this.inGroep && this.data.groep.value) {
            observable = this.identityService.listUsersInGroup(this.data.groep.value.id);
        } else {
            observable = this.identityService.listUsers();
        }
        observable.subscribe(medewerkers => {
            this.medewerkers = medewerkers;
            const validators: ValidatorFn[] = [];
            validators.push(AutocompleteValidators.optionInList(medewerkers));
            if(this.data.medewerker.hasValidator(Validators.required)){
                validators.push(Validators.required);
            }
            this.data.medewerker.setValidators(validators);
            this.filteredMedewerkers = this.data.medewerker.valueChanges.pipe(
                startWith(''),
                map(medewerker => (medewerker ? this._filterMedewerkers(medewerker) : this.medewerkers.slice()))
            );
        });
    }

    displayFn(obj: User | Group): string {
        return obj && obj.naam ? obj.naam : '';
    }

    private _filterGroepen(value: string | Group): Group[] {
        if (typeof value === 'object') {
            return [value];
        }
        const filterValue = value.toLowerCase();
        return this.groepen.filter(groep => groep.naam.toLowerCase().includes(filterValue));
    }

    private _filterMedewerkers(value: string | User): User[] {
        if (typeof value === 'object') {
            return [value];
        }
        const filterValue = value.toLowerCase();
        return this.medewerkers.filter(medewerker => medewerker.naam.toLowerCase().includes(filterValue));
    }
}
