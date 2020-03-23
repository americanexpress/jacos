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

package com.americanexpress.jacos.part;

import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;
import com.americanexpress.jacos.commons.RefreshTokenCallback;
import com.americanexpress.jacos.exception.PartApiException;
import com.americanexpress.jacos.operations.AbstractOperation;
import com.americanexpress.jacos.operations.OperationTypeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * <p>
 * Part Client is the main client object responsible to do all non-bulk sfdc call handling, which includes single SFDC RREST calls, Sobject Collection REST calls
 * <p>
 * T is required for refresh token callback, refresh token callback requires a helper object so that the implementor
 * of refresh token callback can
 * write appropriate lambda logic
 */
public class PartClient<T> {

    private static final Logger log = LoggerFactory.getLogger(PartClient.class);

    private final String API_VERSION;
    private final String instanceUrl;


    private RestTemplate restTemplate;
    private HttpHeaders restHeaders;
    private RefreshTokenCallback<T> refreshTokenCallBack;
    private T configBean;


    public PartClient(String instanceUrl, RestTemplate restTemplate, HttpHeaders restHeaders, T configBean, RefreshTokenCallback<T> refreshTokenCallBack) {
        this(instanceUrl, restTemplate, restHeaders, configBean, refreshTokenCallBack, "v41.0");
    }

    public PartClient(String instanceUrl, RestTemplate restTemplate, HttpHeaders restHeaders, T configBean, RefreshTokenCallback<T> refreshTokenCallBack, String apiVersion) {
        this.instanceUrl = instanceUrl;
        this.restTemplate = restTemplate;
        this.restHeaders = restHeaders;
        this.API_VERSION = apiVersion;
        this.configBean = configBean;
        this.refreshTokenCallBack = refreshTokenCallBack;
    }

    public HttpHeaders getRestHeaders() {
        return restHeaders;
    }

    public void setRestHeaders(HttpHeaders restHeaders) {
        this.restHeaders = restHeaders;
    }

    public String modifyObject(String objectName, OperationEnumJacos operationEnum, String payload) throws PartApiException {
        return modifyObject(objectName, operationEnum, payload, null);
    }


    public String modifyObject(String objectName, OperationEnumJacos operationEnum, String payload, String objectId) throws PartApiException {
        return fireSalesforceModificationAPI(objectName, OperationTypeFactory.operationType(operationEnum), payload, objectId);
    }

    public String fireSalesforceModificationAPI(String objectName, AbstractOperation operationType, String payload, String objectId) throws PartApiException {

        log.info("inside fireSalesforceModificationAPI");

        String restUrl = buildUrl(operationType.createRelativeUrl(objectName, objectId));
        // Create Http headers and add the oauth token to them

        //Queue Based Architecture to refresh salesforce access token expiration. Check link: https://success.salesforce.com/answers?id=9063A0000019iTMQAY

        /* To invalidate and replicate token expiration
        List<String> dummyTest = new ArrayList<String>();
        dummyTest.add("Bearer abc");
        this.restHeaders.replace("Authorization",dummyTest);
        */

        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            ObjectMapper mapper = new ObjectMapper();
            final JsonNode actualObj = mapper.readTree(payload);


            HttpEntity<?> restRequest = new HttpEntity<>(actualObj, restHeaders);

            log.info(String.format("Rest URL is : %s", restUrl));

            // Make a request and read the response string
            final Queue<OperationInfo> operations = new ArrayDeque<>();

            OperationInfo info = new OperationInfo(restUrl, operationType.getRequestType(), restRequest);
            String responseBody = null;

            //final RestUtils.ConfigBean configBean = new RestUtils.ConfigBean(accessTokenConfig.getGrantType(),accessTokenConfig.getRefreshToken(),accessTokenConfig.getClientId(),accessTokenConfig.getClientSecret(),accessTokenConfig.getContentType(),accessTokenConfig.getAccesstokenURI());

            restTemplate.setErrorHandler(new CustomRestErrorHandler() {

                @Override
                public Queue<OperationInfo> getOperations() {
                    return operations;
                }

                @Override
                public boolean hasError(ClientHttpResponse response)
                        throws IOException {
                    try {
                        return super.hasError(response);
                    } catch (Exception e) {
                        log.error("HasError: Exception [" + e.getMessage() + "] occurred while trying to send the request", e);
                        return true;
                    }
                }

                @Override
                public void handleError(ClientHttpResponse response)
                        throws IOException {
                    try {
                        super.handleError(response);
                        log.info(getResponseBodyAsString(response).toString());
                    } catch (HttpClientErrorException e) {

                        if (e.getResponseBodyAsString().contains("INVALID_SESSION_ID")) {
                            OperationInfo operationInfo = refreshTokenHandler(operationType, restUrl, actualObj);
                            getOperations().add(operationInfo);
                            //this.setSuccesResult(callSFDCAPI(operationType,getOperations()));
                            return;
                        }

                        log.error("HttpClientErrorException: Exception [" + e.getResponseBodyAsString() + "] occurred while trying to send the request", e);
                        throw e;
                    } catch (Exception e) {
                        log.error("handleError: Exception [" + e.getMessage() + "] occurred while trying to send the request", e);
                        throw e;
                    }
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
            });


            operations.add(info);
            while (!operations.isEmpty()) {
                try {
                    responseBody = callSFDCAPI(operationType, operations);
                } catch (RestClientException ex) {
                    String succesResult = ((CustomRestErrorHandler) restTemplate.getErrorHandler()).getSuccesResult();
                    if (succesResult != null && !("".equalsIgnoreCase(succesResult))) {
                        responseBody = succesResult;
                    } else {
                        throw new PartApiException("SDFC call failed", ex);
                    }
                }
            }


            //log.info("REST API response header:" + responseStatus);

            return responseBody;
        } catch (IOException ex) {
            //throw  new
            throw new PartApiException("Exception occured while firing SalesForce REST API", ex);
        }
    }

    private String callSFDCAPI(AbstractOperation operationType, Queue<OperationInfo> operations) throws IOException {

        String responseBody;

        OperationInfo info = operations.remove();
        ResponseEntity<String> responseStr = restTemplate.exchange(info.getRestUrl(), info.getRequestMethod(), info.getRestRequest(),
                String.class);

        // Return just the body of the response. You can examine the headers, etc if you wish
        log.info("REST API response header:" + responseStr.getHeaders());
        log.info("REST API response statuscode:" + responseStr.getStatusCode());


        HttpStatus responseStatus = responseStr.getStatusCode();
        HttpHeaders responseHeaders = responseStr.getHeaders();
        responseBody = operationType.getResponse(responseStr);

        log.info("REST API response:" + responseBody);

        return responseBody;
    }

    private OperationInfo refreshTokenHandler(AbstractOperation operationType, String restUrl, JsonNode actualObj) {
        log.info("inside refresh token handler");
        HttpHeaders newrestHeaders = new HttpHeaders();
        restHeaders.entrySet().forEach(
                stringListEntry -> {
                    if (stringListEntry.getKey().equalsIgnoreCase("Authorization")) {
                        String newAccestoken = refreshTokenCallBack.refershToken(restTemplate, configBean);
                        newrestHeaders.add("Authorization", "Bearer " + newAccestoken);
                    } else {
                        newrestHeaders.add(stringListEntry.getKey(), stringListEntry.getValue().get(0));
                    }

                }
        );
        restHeaders = newrestHeaders;
        HttpEntity<?> newrestRequest = new HttpEntity<>(actualObj, restHeaders);
        return new OperationInfo(restUrl, operationType.getRequestType(), newrestRequest);
    }

    private String buildUrl(String path) {
        boolean hasTrailingSlash = instanceUrl.endsWith("/");

        return instanceUrl + (hasTrailingSlash ? "/" : "") + path.replace("vXX.X", API_VERSION);
    }

    private class CustomRestErrorHandler extends DefaultResponseErrorHandler {

        private String succesResult;

        public String getSuccesResult() {
            return succesResult;
        }

        public void setSuccesResult(String succesResult) {
            this.succesResult = succesResult;
        }

        public Queue<OperationInfo> getOperations() {
            return null;
        }


    }

    class OperationInfo {
        String restUrl;
        HttpMethod requestMethod;
        HttpEntity<?> restRequest;


        public OperationInfo(String restUrl, HttpMethod requestMethod, HttpEntity<?> restRequest) {
            this.restUrl = restUrl;
            this.requestMethod = requestMethod;
            this.restRequest = restRequest;
        }

        public String getRestUrl() {
            return restUrl;
        }

        public void setRestUrl(String restUrl) {
            this.restUrl = restUrl;
        }

        public HttpMethod getRequestMethod() {
            return requestMethod;
        }

        public void setRequestMethod(HttpMethod requestMethod) {
            this.requestMethod = requestMethod;
        }

        public HttpEntity<?> getRestRequest() {
            return restRequest;
        }

        public void setRestRequest(HttpEntity<?> restRequest) {
            this.restRequest = restRequest;
        }
    }
}
