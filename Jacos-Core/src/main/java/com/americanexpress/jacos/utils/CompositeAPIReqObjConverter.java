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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.americanexpress.jacos.model.CompositeAPIRequest.Record;

/**
 * Accept data and header and returns the CompositeAPIRequest.Record.
 *
 */
public class CompositeAPIReqObjConverter implements Function<String[], Record> {

    public static final String ATTRIBUTE_CONSTANT = "type";
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeAPIReqObjConverter.class);
    private final String[] header;
    private final Map<String, String> attributes;

    public CompositeAPIReqObjConverter(String[] header, String attrType) {
        this.header = header;
        attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_CONSTANT, attrType);
    }


    public CompositeAPIReqObjConverter(String[] header, HashMap<String, String> attributes) {
        this.header = header;
        this.attributes = attributes;
    }


    @Override
    public Record apply(String[] data) {
        LOGGER.debug("Processing:{}", data);
        if (Objects.isNull(data) || header.length != data.length) {
            throw new IllegalStateException("Header and data not in sync. Header:" + Arrays.toString(header) + " , Data:" + Arrays.toString(data));
        }

        Record record = new Record();
        Map<String, String> fields = new HashMap<>();
        record.setAttributes(attributes);
        for (int i = 0; i < header.length; i++) {
            fields.put(header[i], data[i]);
        }
        record.setFields(fields);
        LOGGER.debug("Obj:{}", record);
        return record;
    }
}
