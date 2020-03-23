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

import com.americanexpress.jacos.model.CompositeAPIRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * The custom JsonSerializer creates json as per the salesforce multiupdate sobject spec.
 * It iterate through each Record and convert Json in salesforce accepted format.</p>
 * <br>
 * <b>e.g. </b>
 * "records" : [{
 * "attributes" : {"type" : "Account"},
 * "id" : "1234",
 * "NumberOfEmployees" : 100
 * },{
 * "attributes" : {"type" : "Contact"},
 * "id" : "12345",
 * "Title" : "Lead Engineer"
 * }]
 */

public class CompositeAPIReqJsonSerializer extends JsonSerializer<CompositeAPIRequest.Record[]> {
    public void serialize(CompositeAPIRequest.Record[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();

        for (CompositeAPIRequest.Record record : value) {
            jgen.writeStartObject();
            jgen.writeObjectField("attributes", record.getAttributes());
            for (Map.Entry<String, String> entry : record.getFields().entrySet()) {
                jgen.writeStringField(entry.getKey(), Objects.isNull(entry.getValue()) || entry.getValue().isEmpty() ? null : entry.getValue());
            }
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
        ;
    }
}