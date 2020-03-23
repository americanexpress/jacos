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

package com.americanexpress.jacos.operations;

import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;


public class OperationTypeFactory {

    public static AbstractOperation operationType(String operation) {
        try {
            OperationEnumJacos value = OperationEnumJacos.valueOf(operation.toUpperCase());
            return operationType(value);

        } catch (IllegalArgumentException ex) {
            return new NullableOperation(operation);
        }

    }


    public static AbstractOperation operationType(OperationEnumJacos operationEnum) {
        AbstractOperation operationType = null;
        switch (operationEnum) {
            case INSERT:
                operationType = new InsertOperation();
                break;
            case UPDATE:
                operationType = new UpdateOperation();
                break;
            case UPSERT:
                operationType = new UpsertOperation();
                break;
            case DELETE:
                operationType = new DeleteOperation();
                break;
            case MULTIINSERT:
                operationType = new MultiRecordInsertOperation();
                break;
            case MULTIUPDATE:
                operationType = new MultiRecordUpdateOperation();
                break;
            /*
             * CustomRestAPI is the Operation type for Salesforce Custom Rest API call.
             */
            case CUSTOMRESTAPI:
                operationType = new CustomRestAPI();
                break;

        }
        return operationType;
    }
}
