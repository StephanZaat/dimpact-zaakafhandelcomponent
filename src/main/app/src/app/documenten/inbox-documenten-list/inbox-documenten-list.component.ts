/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {UtilService} from '../../core/service/util.service';
import {InboxDocumentenService} from '../inbox-documenten.service';
import {AfterViewInit, Component, EventEmitter, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge} from 'rxjs';
import {map, startWith, switchMap} from 'rxjs/operators';
import {InformatieObjectenService} from '../../informatie-objecten/informatie-objecten.service';
import {MatTableDataSource} from '@angular/material/table';
import {SessionStorageUtil} from '../../shared/storage/session-storage.util';
import {EnkelvoudigInformatieobject} from '../../informatie-objecten/model/enkelvoudig-informatieobject';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../shared/confirm-dialog/confirm-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {InboxDocument} from '../model/inbox-document';
import {InformatieObjectVerplaatsService} from '../../informatie-objecten/informatie-object-verplaats.service';
import {InboxDocumentListParameters} from '../model/inbox-document-list-parameters';

@Component({
    templateUrl: './inbox-documenten-list.component.html',
    styleUrls: ['./inbox-documenten-list.component.less']
})
export class InboxDocumentenListComponent implements OnInit, AfterViewInit {

    isLoadingResults = true;
    dataSource: MatTableDataSource<InboxDocument> = new MatTableDataSource<InboxDocument>();
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    displayedColumns: string[] = ['identificatie', 'creatiedatum', 'titel', 'actions'];
    filterColumns: string[] = ['identificatie_filter', 'creatiedatum_filter', 'titel_filter', 'actions_filter'];
    listParameters: InboxDocumentListParameters;
    filterChange: EventEmitter<void> = new EventEmitter<void>();

    constructor(private inboxDocumentenService: InboxDocumentenService,
                private infoService: InformatieObjectenService,
                private utilService: UtilService,
                public dialog: MatDialog,
                private translate: TranslateService,
                private informatieObjectVerplaatsService: InformatieObjectVerplaatsService) { }

    ngOnInit(): void {
        this.utilService.setTitle('title.documenten.inboxDocumenten');
        this.listParameters = SessionStorageUtil.getItem('inboxDocumenten', this.createDefaultParameters());
    }

    ngAfterViewInit(): void {
        this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
        merge(this.sort.sortChange, this.paginator.page, this.filterChange).pipe(
            startWith({}),
            switchMap(() => {
                this.isLoadingResults = true;
                this.utilService.setLoading(true);
                this.updateListParameters();
                return this.inboxDocumentenService.list(this.listParameters);
            }),
            map(data => {
                this.isLoadingResults = false;
                this.utilService.setLoading(false);
                SessionStorageUtil.setItem('inboxDocumenten', this.listParameters);
                return data;
            })
        ).subscribe(data => {
            this.paginator.length = data.totaal;
            this.dataSource.data = data.resultaten;
        });
    }

    updateListParameters(): void {
        this.listParameters.sort = this.sort.active;
        this.listParameters.order = this.sort.direction;
        this.listParameters.page = this.paginator.pageIndex;
        this.listParameters.maxResults = this.paginator.pageSize;
    }

    getDownloadURL(od: InboxDocument): string {
        return this.infoService.getDownloadURL(od.enkelvoudiginformatieobjectUUID);
    }

    documentVerplaatsen(od: InboxDocument): void {
        this.informatieObjectVerplaatsService.addTeVerplaatsenDocument(InboxDocumentenListComponent.getInformatieobject(od), 'inbox-documenten');
    }

    documentVerwijderen(inboxDocument: InboxDocument): void {
        this.dialog.open(ConfirmDialogComponent, {
            data: new ConfirmDialogData(
                this.translate.instant('msg.document.verwijderen.bevestigen', {document: inboxDocument.titel}),
                this.inboxDocumentenService.delete(inboxDocument)
            )
        }).afterClosed().subscribe(result => {
            if (result) {
                this.utilService.openSnackbar('msg.document.verwijderen.uitgevoerd', {document: inboxDocument.titel});
                this.paginator.page.emit();
            }
        });
    }

    isDocumentVerplaatsenDisabled(inboxDocument: InboxDocument): boolean {
        return this.informatieObjectVerplaatsService.isReedsTeVerplaatsen(InboxDocumentenListComponent.getInformatieobject(inboxDocument));
    }

    private static getInformatieobject(inboxDocument: InboxDocument): EnkelvoudigInformatieobject {
        const informatieobject = new EnkelvoudigInformatieobject();
        informatieobject.titel = inboxDocument.titel;
        informatieobject.uuid = inboxDocument.enkelvoudiginformatieobjectUUID;
        informatieobject.identificatie = inboxDocument.enkelvoudiginformatieobjectID;
        return informatieobject;
    }

    filtersChanged(): void {
        this.paginator.pageIndex = 0;
        this.filterChange.emit();
    }

    resetSearch(): void {
        this.listParameters = this.createDefaultParameters();
        this.filtersChanged();
    }

    createDefaultParameters(): InboxDocumentListParameters {
        return new InboxDocumentListParameters('creatiedatum', 'desc');
    }
}
