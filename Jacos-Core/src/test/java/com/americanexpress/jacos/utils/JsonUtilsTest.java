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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class JsonUtilsTest {

    private static final Logger LOGGER = LogManager.getLogger(JsonUtilsTest.class);


    @Test
    public void testCompositeAPIRequestSerializer() throws JsonProcessingException {

        CompositeAPIRequest test = new CompositeAPIRequest();

        test.setAllOrNone(true);

        CompositeAPIRequest.Record rec = new CompositeAPIRequest.Record();

        rec.setAttributes(new HashMap<>());
        rec.getAttributes().put("type", "Account");
        rec.setFields(new HashMap<>());
        rec.getFields().put("id", "sfdc_id");
        rec.getFields().put("NumberOfEmployees", "100");
        CompositeAPIRequest.Record rec1 = new CompositeAPIRequest.Record();

        rec1.setAttributes(new HashMap<>());
        rec1.getAttributes().put("type", "Contact");
        rec1.setFields(new HashMap<>());
        rec1.getFields().put("id", "sfdc_id");
        rec1.getFields().put("NumberOfEmployees", "");

        test.setRecords(new CompositeAPIRequest.Record[]{rec, rec1});
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String op = mapper.writeValueAsString(test);
        LOGGER.debug(op);

        Assert.assertEquals("{\n" +
                "  \"allOrNone\" : true,\n" +
                "  \"records\" : [ {\n" +
                "    \"attributes\" : {\n" +
                "      \"type\" : \"Account\"\n" +
                "    },\n" +
                "    \"id\" : \"sfdc_id\",\n" +
                "    \"NumberOfEmployees\" : \"100\"\n" +
                "  }, {\n" +
                "    \"attributes\" : {\n" +
                "      \"type\" : \"Contact\"\n" +
                "    },\n" +
                "    \"id\" : \"sfdc_id\",\n" +
                "    \"NumberOfEmployees\" : null\n" +
                "  } ]\n" +
                "}",op);

    }
}
