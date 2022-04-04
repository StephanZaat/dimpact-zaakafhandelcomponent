/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FoutAfhandelingService} from '../fout-afhandeling/fout-afhandeling.service';
import {Observable, Subject} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {EnkelvoudigInformatieobject} from './model/enkelvoudig-informatieobject';
import {ZaakInformatieobject} from './model/zaak-informatieobject';
import {Informatieobjecttype} from './model/informatieobjecttype';
import {HistorieRegel} from '../shared/historie/model/historie-regel';
import {EnkelvoudigInformatieObjectZoekParameters} from './model/enkelvoudig-informatie-object-zoek-parameters';
import {SessionStorageUtil} from '../shared/storage/session-storage.util';
import {UtilService} from '../core/service/util.service';
import {Router} from '@angular/router';
import {ActionIcon} from '../shared/edit/action-icon';
import {DocumentVerplaatsGegevens} from './model/document-verplaats-gegevens';
import {ActionBarAction} from '../core/actionbar/model/action-bar-action';

@Injectable({
    providedIn: 'root'
})
export class InformatieObjectenService {

    private basepath = '/rest/informatieobjecten';

    constructor(private http: HttpClient, private foutAfhandelingService: FoutAfhandelingService, private utilService: UtilService, private router: Router) {
    }

    readEnkelvoudigInformatieobject(uuid: string): Observable<EnkelvoudigInformatieobject> {
        return this.http.get<EnkelvoudigInformatieobject>(`${this.basepath}/informatieobject/${uuid}`).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    listInformatieobjecttypes(zaakTypeID): Observable<Informatieobjecttype[]> {
        return this.http.get<Informatieobjecttype[]>(`${this.basepath}/informatieobjecttypes/${zaakTypeID}`).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    listInformatieobjecttypesForZaak(zaakUUID): Observable<Informatieobjecttype[]> {
        return this.http.get<Informatieobjecttype[]>(`${this.basepath}/informatieobjecttypes/zaak/${zaakUUID}`).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    createEnkelvoudigInformatieobject(zaakUuid: string, infoObject: EnkelvoudigInformatieobject): Observable<EnkelvoudigInformatieobject> {
        return this.http.post<EnkelvoudigInformatieobject>(`${this.basepath}/informatieobject/${zaakUuid}`, infoObject).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    listEnkelvoudigInformatieobjecten(zoekParameters: EnkelvoudigInformatieObjectZoekParameters): Observable<EnkelvoudigInformatieobject[]> {
        return this.http.put<EnkelvoudigInformatieobject[]>(`${this.basepath}/informatieobjectenList`, zoekParameters).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    listZaakInformatieobjecten(uuid: string): Observable<ZaakInformatieobject[]> {
        return this.http.get<ZaakInformatieobject[]>(`${this.basepath}/informatieobject/${uuid}/zaken`).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    listHistorie(uuid: string): Observable<HistorieRegel[]> {
        return this.http.get<HistorieRegel[]>(`${this.basepath}/informatieobject/${uuid}/historie`).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    getDownloadURL(uuid: string): string {
        return `${this.basepath}/informatieobject/${uuid}/download`;
    }

    getUploadURL(uuid: string): string {
        return `${this.basepath}/informatieobject/upload/${uuid}`;
    }

    postVerplaatsDocument(documentVerplaatsGegevens: DocumentVerplaatsGegevens, nieuweZaakID: string): Observable<void> {
        documentVerplaatsGegevens.nieuweZaakID = nieuweZaakID;
        return this.http.post<void>(`${this.basepath}/informatieobject/verplaats`, documentVerplaatsGegevens).pipe(
            catchError(err => this.foutAfhandelingService.redirect(err))
        );
    }

    addTeVerplaatsenDocument(informatieobject: EnkelvoudigInformatieobject, zaakIdentificatie: string): void {
        if (!this.isReedsTeVerplaatsen(informatieobject)) {
            this._verplaatsenDocument(new DocumentVerplaatsGegevens(informatieobject.uuid, informatieobject.titel, zaakIdentificatie));
        }
    }

    isReedsTeVerplaatsen(informatieobject: EnkelvoudigInformatieobject): boolean {
        const teVerplaatsenDocumenten = SessionStorageUtil.getItem('teVerplaatsenDocumenten', []) as DocumentVerplaatsGegevens[];
        return teVerplaatsenDocumenten.find(dvg => dvg.documentUUID === informatieobject.uuid) !== undefined;
    }

    appInit() {
        const documenten = SessionStorageUtil.getItem('teVerplaatsenDocumenten', []) as DocumentVerplaatsGegevens[];
        documenten.forEach(document => {
            this._verplaatsenDocument(document, true);
        });
    }

    private _verplaatsenDocument(document: DocumentVerplaatsGegevens, onInit?: boolean) {
        const dismiss: Subject<void> = new Subject<void>();
        dismiss.asObservable().subscribe(() => {
            this.deleteTeVerplaatsenDocument(document);
        });
        const verplaatsAction = new Subject<string>();
        verplaatsAction.asObservable().subscribe(url => {
            const nieuweZaakID = url.split('/').pop();
            this.postVerplaatsDocument(document, nieuweZaakID).subscribe(() =>
                this.utilService.openSnackbar('actie.document.verplaatsen.uitgevoerd')
            );
            this.deleteTeVerplaatsenDocument(document);
        });
        const teVerplaatsenDocumenten = SessionStorageUtil.getItem('teVerplaatsenDocumenten', []) as DocumentVerplaatsGegevens[];
        teVerplaatsenDocumenten.push(document);
        if (!onInit) {
            SessionStorageUtil.setItem('teVerplaatsenDocumenten', teVerplaatsenDocumenten);
        }
        const action: ActionBarAction = new ActionBarAction(document.documentTitel, 'document', document.zaakID,
            new ActionIcon('content_paste_go', verplaatsAction), dismiss, () => this.isVerplaatsenToegestaan(document));
        this.utilService.addAction(action);
    }

    private deleteTeVerplaatsenDocument(documentVerplaatsGegevens: DocumentVerplaatsGegevens) {
        const documenten = SessionStorageUtil.getItem('teVerplaatsenDocumenten', []) as DocumentVerplaatsGegevens[];
        SessionStorageUtil.setItem('teVerplaatsenDocumenten', documenten.filter(document => document.documentUUID !== documentVerplaatsGegevens.documentUUID));
    }

    private pathContains(path: string): boolean {
        return this.router.url.indexOf(path) !== -1;
    }

    private isZaakTonen(): boolean {
        return this.pathContains('zaken/ZAAK-');
    }

    private isVerplaatsenToegestaan(gegevens: DocumentVerplaatsGegevens): boolean {
        return this.isZaakTonen() && !this.pathContains(`zaken/${gegevens.zaakID}`);
    }
}
