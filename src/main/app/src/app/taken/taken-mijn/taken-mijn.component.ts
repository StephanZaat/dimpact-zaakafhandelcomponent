/*
 * SPDX-FileCopyrightText: 2021 - 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Taak} from '../model/taak';
import {MatButtonToggle} from '@angular/material/button-toggle';
import {ActivatedRoute} from '@angular/router';
import {TakenService} from '../taken.service';
import {UtilService} from '../../core/service/util.service';
import {TakenMijnDatasource} from './taken-mijn-datasource';
import {detailExpand} from '../../shared/animations/animations';
import {Conditionals} from '../../shared/edit/conditional-fn';
import {TextIcon} from '../../shared/edit/text-icon';

@Component({
    templateUrl: './taken-mijn.component.html',
    styleUrls: ['./taken-mijn.component.less'],
    animations: [detailExpand]
})
export class TakenMijnComponent implements AfterViewInit, OnInit {

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    @ViewChild(MatTable) table: MatTable<Taak>;
    @ViewChild('toggleColumns') toggleColumns: MatButtonToggle;

    dataSource: TakenMijnDatasource;
    expandedRow: Taak | null;

    displayedColumns: string[] = ['naam', 'creatiedatumTijd'];

    // columnNaam: TableColumn;
    // columnStatus: TableColumn;
    // columnZaakIdentificatie: TableColumn;
    // columnZaaktypeOmschrijving: TableColumn;
    // columnCreatieDatum: TableColumn;
    // columnStreefDatum: TableColumn;
    // columnGroep: TableColumn;
    // columnUrl: TableColumn;

    streefdatumIcon: TextIcon;

    constructor(private route: ActivatedRoute, private takenService: TakenService, public utilService: UtilService) {
    }

    ngOnInit() {
        this.utilService.setTitle('title.taken.mijn');
        this.dataSource = new TakenMijnDatasource(this.takenService, this.utilService);

        // this.columnNaam = new TableColumn('naam', 'naam', true, TaakSortering.TAAKNAAM);
        // this.columnStatus = new TableColumn('status', 'status', true);
        // this.columnZaakIdentificatie = new TableColumn('zaakIdentificatie', 'zaakIdentificatie', true);
        // this.columnZaaktypeOmschrijving = new TableColumn('zaaktypeOmschrijving', 'zaaktypeOmschrijving', true);
        // this.columnCreatieDatum = new TableColumn('creatiedatumTijd', 'creatiedatumTijd', true,
        //     TaakSortering.CREATIEDATUM);//.pipe(DatumPipe);
        // this.columnStreefDatum = new TableColumn('streefdatum', 'streefdatum', true, TaakSortering.STREEFDATUM);
        // this.columnGroep = new TableColumn('groep', 'groep.naam', true);
        // this.columnUrl = new TableColumn('url', 'url', true, null, true);

        this.dataSource.columns = [
            // this.columnNaam,
            // this.columnStatus,
            // this.columnZaakIdentificatie,
            // this.columnZaaktypeOmschrijving,
            // this.columnCreatieDatum,
            // this.columnStreefDatum,
            // this.columnGroep,
            // this.columnUrl
        ];
    }

    ngAfterViewInit(): void {
        this.dataSource.setViewChilds(this.paginator, this.sort);
        this.dataSource.load();
        this.table.dataSource = this.dataSource;

        this.streefdatumIcon = new TextIcon(Conditionals.isAfterDate(), 'report_problem', 'warningTaakVerlopen_icon',
            'msg.datum.overschreden', 'warning');
    }

    isAfterDate(datum): boolean {
        return Conditionals.isOverschreden(datum);
    }
}
