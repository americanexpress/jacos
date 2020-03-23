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

package com.americanexpress.jacos.model;

import com.americanexpress.jacos.utils.CompositeAPIReqJsonSerializer;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

/**
 * The Salesforce composite sobject request schema.
 *
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_sobjects_collections_update.htm">composite_sobjects</a>
 */

@JsonPropertyOrder({"allOrNone", "records"})
public class CompositeAPIRequest {

    @JsonSerialize(using = CompositeAPIReqJsonSerializer.class)
    @JsonProperty("records")
    Record[] Records;
    private boolean allOrNone;

    public boolean isAllOrNone() {
        return allOrNone;
    }

    public void setAllOrNone(boolean allOrNone) {
        this.allOrNone = allOrNone;
    }

    @JsonIgnore
    public Record[] getRecords() {
        return Records;
    }

    @JsonIgnore
    public void setRecords(Record[] records) {
        Records = records;
    }

    public static class Record {
        Map<String, String> attributes;

        Map<String, String> fields;

        public Map<String, String> getFields() {
            return fields;
        }

        @JsonAnySetter
        public void setFields(Map<String, String> fields) {
            this.fields = fields;
        }

        @JsonAnyGetter
        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "attributes=" + attributes +
                    ", fields=" + fields +
                    '}';
        }
    }
}
