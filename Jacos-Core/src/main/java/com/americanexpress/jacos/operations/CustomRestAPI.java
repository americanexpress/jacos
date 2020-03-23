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

/**
 * Class ::  CustomRestAPI
 * Desperation :: Class to connect with custom salesforce api add read the value
 */


import com.americanexpress.jacos.model.CustomMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class CustomRestAPI extends AbstractOperation {

    private static final Logger logger = LogManager.getLogger(CustomRestAPI.class);
    private HttpMethod method;

    @Override
    protected String baseServicePath() {
        return "/services/apexrest/";
    }

    @Override
    protected String formUrlfromBaseUrl(String strURI, String metadata) {
        String url = baseServicePath() + strURI;
        ObjectMapper mapper = new ObjectMapper();

        CustomMetadata customMetadata = null;
        try {
            customMetadata = mapper.readValue(metadata, CustomMetadata.class);
            method = HttpMethod.valueOf(customMetadata.getMethod());

        } catch (IOException e) {
            logger.error(e);
        }


        return url;
    }

    @Override
    public HttpMethod getRequestType() {
        return method;
    }

    @Override
    public String getResponse(ResponseEntity<String> responseStr) {
        return responseStr.getBody();
    }
}