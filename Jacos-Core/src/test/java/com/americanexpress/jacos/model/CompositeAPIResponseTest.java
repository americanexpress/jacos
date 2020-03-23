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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_sobjects_collections_update.htm">salesforce</a>
 * @since 1.0
 */
public class CompositeAPIResponseTest {

    public static final String responseNoError = "[\n" +
            "   {\n" +
            "      \"id\" : \"sfdcid_abc\",\n" +
            "      \"success\" : true,\n" +
            "      \"errors\" : [ ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"id\" : \"sfdcid_abc\",\n" +
            "      \"success\" : true,\n" +
            "      \"errors\" : [ ]\n" +
            "   }\n" +
            "]";
    public static final String responseWithError = "[\n" +
            "   {\n" +
            "      \"id\" : \"sfdcid_abc\",\n" +
            "      \"success\" : true,\n" +
            "      \"errors\" : [ ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"success\" : false,\n" +
            "      \"errors\" : [\n" +
            "         {\n" +
            "            \"statusCode\" : \"MALFORMED_ID\",\n" +
            "            \"message\" : \"Contact ID: id value of incorrect type: sfdcid_abc\",\n" +
            "            \"fields\" : [\n" +
            "               \"Id\"\n" +
            "            ]\n" +
            "         }\n" +
            "      ]\n" +
            "   }\n" +
            "]";
    public static final String responseAllOrNoTrue = "[\n" +
            "   {\n" +
            "      \"id\" : \"sfdcid_abc\",\n" +
            "      \"success\" : false,\n" +
            "      \"errors\" : [\n" +
            "         {\n" +
            "            \"statusCode\" : \"ALL_OR_NONE_OPERATION_ROLLED_BACK\",\n" +
            "            \"message\" : \"Record rolled back because not all records were valid and the request was using AllOrNone header\",\n" +
            "            \"fields\" : [ ]\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"success\" : false,\n" +
            "      \"errors\" : [\n" +
            "         {\n" +
            "            \"statusCode\" : \"MALFORMED_ID\",\n" +
            "            \"message\" : \"Contact ID: id value of incorrect type: sfdcid_abc\",\n" +
            "            \"fields\" : [\n" +
            "               \"Id\"\n" +
            "            ]\n" +
            "         }\n" +
            "      ]\n" +
            "   }\n" +
            "]";
    private static final Logger LOGGER = LogManager.getLogger(CompositeAPIResponseTest.class);
    public static String ts = "[\"a\",\n" +
            "\"b\"]";
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserlizeNoError() throws IOException {

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CompositeAPIResponse[] response = mapper.readValue(responseNoError, new TypeReference<CompositeAPIResponse[]>() {
        });

        LOGGER.debug("Objs:{}", Arrays.toString(response));


        Assert.assertEquals(response.length, 2);
        Assert.assertNotNull(response[0].getErrors());
        Assert.assertEquals(response[0].getErrors().length, 0);


    }

    @Test
    public void testDeserlizeWithError() throws IOException {

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CompositeAPIResponse[] response = mapper.readValue(responseWithError, new TypeReference<CompositeAPIResponse[]>() {
        });

        LOGGER.debug("Objs:{}", Arrays.toString(response));
        Assert.assertEquals(response.length, 2);
        Assert.assertNotNull(response[0].getErrors());
        Assert.assertEquals(response[0].getErrors().length, 0);
        Assert.assertEquals(response[1].getErrors().length, 1);
    }


    @Test
    public void testDeserlizeAllOrNoneError() throws IOException {

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CompositeAPIResponse[] response = mapper.readValue(responseAllOrNoTrue, new TypeReference<CompositeAPIResponse[]>() {
        });


        LOGGER.debug("Objs:{}", Arrays.toString(response));
        Assert.assertEquals(response.length, 2);
        Assert.assertNotNull(response[0].getErrors());
        Assert.assertEquals(response[0].getErrors().length, 1);
        Assert.assertEquals(response[1].getErrors().length, 1);
        Assert.assertEquals(response[1].getErrors()[0].getFields().length, 1);
    }

    @Test
    public void testDeserlize1() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String[] values = mapper.readValue(ts, new TypeReference<String[]>() {
        });
        System.out.println(Arrays.toString(values));
    }
}
