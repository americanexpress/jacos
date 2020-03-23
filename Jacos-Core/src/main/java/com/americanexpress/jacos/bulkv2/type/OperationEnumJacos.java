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

package com.americanexpress.jacos.bulkv2.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum OperationEnumJacos {

    INSERT("insert"),

    DELETE("delete"),

    UPDATE("update"),

    UPSERT("upsert"),

    MULTIINSERT("multiinsert"),

    MULTIUPDATE("multiupdate"),

    // This is Operation is to call custom salesforce API
    CUSTOMRESTAPI("customrestapi");

    private final String value;

    OperationEnumJacos(String value) {
        this.value = value;
    }

    @JsonCreator
    public static OperationEnumJacos fromValue(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }

    @JsonValue
    public String toJsonValue() {
        return value;
    }
}
