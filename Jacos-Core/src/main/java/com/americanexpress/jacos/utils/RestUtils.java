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

import com.americanexpress.jacos.exception.BulkConnectorException;
import com.americanexpress.jacos.oauth2.configuration.OAuthGetAccessTokenConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

public class RestUtils {

    public static final String ACCESSTOKEN_KEY = "access_token";
    private static final Logger logger = LogManager.getLogger(RestUtils.class);

    private static ResponseEntity<String> callForAccessToken(RestTemplate restTemplate, ConfigBean configBean) {
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", configBean.getGrantType());
        bodyMap.add("refresh_token", configBean.getRefreshToken());
        bodyMap.add("client_id", configBean.getClientId());
        bodyMap.add("client_secret", configBean.getClientSecret());
        logger.info("Getting the access token during the application startup:{}", bodyMap);

        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isEmpty(configBean.getContentType())) {
            headers.setContentType(MediaType.valueOf(configBean.getContentType().toUpperCase()));
        }
        return restTemplate.exchange(configBean.getAccesstokenURI(), HttpMethod.POST,
                new HttpEntity(bodyMap, headers), String.class);
    }

    public static synchronized String getFreshAccessToken(RestTemplate restTemplate, ConfigBean configBean) {
        ResponseEntity<String> accessTokenResponse = callForAccessToken(restTemplate, configBean);
        if (HttpStatus.OK == accessTokenResponse.getStatusCode()) {
            /*Gson json = new Gson();
            return json.fromJson(accessTokenResponse.getBody(), HashMap.class).get(ACCESSTOKEN_KEY).toString();*/
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(accessTokenResponse.getBody(), new TypeReference<Map<String, String>>() {
                });
                return map.get(ACCESSTOKEN_KEY).toString();
            } catch (IOException ex) {
                throw new BulkConnectorException("Exception while... fetching the Fresh Access Token", ex);
            }

        }
        return null;
    }

    private static ResponseEntity<String> callForAccessToken(RestTemplate restTemplate, OAuthGetAccessTokenConfig authGetAccessTokenConfig) {
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", authGetAccessTokenConfig.getGrantType());
        bodyMap.add("refresh_token", authGetAccessTokenConfig.getRefreshToken());
        bodyMap.add("client_id", authGetAccessTokenConfig.getClientId());
        bodyMap.add("client_secret", authGetAccessTokenConfig.getClientSecret());
        logger.info("Getting the access token during the application startup:{}", bodyMap);

        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isEmpty(authGetAccessTokenConfig.getContentType())) {
            headers.setContentType(MediaType.valueOf(authGetAccessTokenConfig.getContentType().toUpperCase()));
        }
        return restTemplate.exchange(authGetAccessTokenConfig.getAccesstokenURI(), HttpMethod.POST,
                new HttpEntity(bodyMap, headers), String.class);
    }

    public static synchronized String getFreshAccessToken(RestTemplate restTemplate, OAuthGetAccessTokenConfig authGetAccessTokenConfig) {
        ResponseEntity<String> accessTokenResponse = callForAccessToken(restTemplate, authGetAccessTokenConfig);
        if (HttpStatus.OK == accessTokenResponse.getStatusCode()) {
            /*Gson json = new Gson();
            return json.fromJson(accessTokenResponse.getBody(), HashMap.class).get(ACCESSTOKEN_KEY).toString();*/
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(accessTokenResponse.getBody(), new TypeReference<Map<String, String>>() {
                });
                return map.get(ACCESSTOKEN_KEY).toString();
            } catch (IOException ex) {
                throw new BulkConnectorException("Exception while... fetching the Fresh Access Token", ex);
            }

        }
        return null;
    }

    public static class ConfigBean {
        private String grantType;
        private String refreshToken;
        private String clientId;
        private String clientSecret;
        private String contentType;
        private String accesstokenURI;

        public ConfigBean(String grantType, String refreshToken, String clientId, String clientSecret, String contentType, String accesstokenURI) {
            this.grantType = grantType;
            this.refreshToken = refreshToken;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.contentType = contentType;
            this.accesstokenURI = accesstokenURI;
        }

        public String getGrantType() {
            return grantType;
        }

        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getAccesstokenURI() {
            return accesstokenURI;
        }

        public void setAccesstokenURI(String accesstokenURI) {
            this.accesstokenURI = accesstokenURI;
        }
    }
}
