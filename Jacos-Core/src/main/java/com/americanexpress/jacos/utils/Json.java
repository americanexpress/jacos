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

package com.americanexpress.jacos.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class Json {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Json() {
    }

    public static String encode(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T decode(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T decode(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
