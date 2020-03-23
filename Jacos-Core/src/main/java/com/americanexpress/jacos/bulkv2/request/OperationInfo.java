/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.americanexpress.jacos.bulkv2.request;

import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;

import java.io.Serializable;

public class OperationInfo implements Serializable {

    private static final long serialVersionUID = 874645678776025530L;
    private String objectType;
    private OperationEnumJacos operationType;
    private String externalIdField;

    public OperationInfo(String objectType, OperationEnumJacos operationType, String externalIdField) {
        this.objectType = objectType;
        this.operationType = operationType;
        this.externalIdField = externalIdField;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public OperationEnumJacos getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationEnumJacos operationType) {
        this.operationType = operationType;
    }

    public String getExternalIdField() {
        return externalIdField;
    }

    public void setExternalIdField(String externalIdField) {
        this.externalIdField = externalIdField;
    }
}
