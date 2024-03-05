/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import { AbstractFormControlField } from "../../model/abstract-form-control-field";
import { FieldType } from "../../model/field-type.enum";

export class ReadonlyFormField extends AbstractFormControlField {
  fieldType = FieldType.READONLY;

  constructor() {
    super();
  }
}
