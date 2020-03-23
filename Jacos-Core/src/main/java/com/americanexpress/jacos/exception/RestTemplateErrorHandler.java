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

package com.americanexpress.jacos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

public class RestTemplateErrorHandler implements ResponseErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {

        return (
                (clientHttpResponse.getStatusCode().series() == CLIENT_ERROR ||
                        clientHttpResponse.getStatusCode().series() == SERVER_ERROR) && !((clientHttpResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED) && (getResponseBodyAsString(clientHttpResponse).toString().contains("INVALID_SESSION_ID"))))
        );
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

        traceResponse(clientHttpResponse);

    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        StringBuilder inputStringBuilder = getResponseBodyAsString(response);
        log.error("============================Failed response begin==========================================");
        log.error("Status code  : {}", response.getStatusCode());
        log.error("Status text  : {}", response.getStatusText());
        log.error("Headers      : {}", response.getHeaders());
        log.error("Response body: {}", inputStringBuilder.toString());
        log.error("=======================Failed response end=================================================");
    }

    private StringBuilder getResponseBodyAsString(ClientHttpResponse response) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        while (line != null) {
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }
        return inputStringBuilder;
    }
}
