/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { AbstractFileFormField } from "../../model/abstract-file-form-field";
import { FieldType } from "../../model/field-type.enum";

export class FileFormField extends AbstractFileFormField {
  fieldType: FieldType = FieldType.FILE;

  constructor() {
    super();
  }
}
