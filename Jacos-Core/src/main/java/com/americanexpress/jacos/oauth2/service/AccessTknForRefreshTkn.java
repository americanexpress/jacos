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

package com.americanexpress.jacos.oauth2.service;

import com.americanexpress.jacos.exception.BulkConnectorException;
import com.americanexpress.jacos.exception.RestTemplateErrorHandler;
import com.americanexpress.jacos.oauth2.configuration.OAuthGetAccessTokenConfig;
import com.americanexpress.jacos.utils.RestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Service("accessTknForRefreshTkn")
@ConfigurationProperties(prefix = "jacos.sfdc.proxy")
public class AccessTknForRefreshTkn {
    private static final Logger logger = LogManager.getLogger(AccessTknForRefreshTkn.class);
    @Autowired
    private OAuthGetAccessTokenConfig accessTknConfig;
    private HttpHeaders headers = new HttpHeaders();
    private MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();

    private String accessToken;
    private RestTemplate restTemplate;
    private boolean proxyEnabled;
    private String host;
    private Integer port;
    private String userName;
    private String password;
    private Integer timeout;

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private void initializeRestTemplate() {
        if (proxyEnabled) {
            boolean isCredentialRequired = (userName != null && password != null);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            if (isCredentialRequired) {
                credsProvider.setCredentials(
                        new AuthScope(host, port),
                        new UsernamePasswordCredentials(userName, password));
            }


            HttpHost myProxy = new HttpHost(host, port);
            HttpClient client = isCredentialRequired ? HttpClientBuilder.create().
                    setProxy(myProxy). //build();
                    setDefaultCredentialsProvider(credsProvider).disableCookieManagement().build()
                    : HttpClientBuilder.create().
                    setProxy(myProxy).build();
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
            clientHttpRequestFactory.setConnectTimeout(timeout);
            clientHttpRequestFactory.setConnectionRequestTimeout(timeout);
            clientHttpRequestFactory.setReadTimeout(timeout);
            this.restTemplate = new RestTemplate(clientHttpRequestFactory);
        } else {
            this.restTemplate = new RestTemplate();
        }
        this.restTemplate.setErrorHandler(new RestTemplateErrorHandler());
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing the Rest Template based on the proxy settings");
        initializeRestTemplate();
        if (!StringUtils.isEmpty(accessTknConfig.getContentType())) {
            headers.setContentType(MediaType.valueOf(accessTknConfig.getContentType().toUpperCase()));
        }
        bodyMap.add("grant_type", accessTknConfig.getGrantType());
        bodyMap.add("refresh_token", accessTknConfig.getRefreshToken());
        bodyMap.add("client_id", accessTknConfig.getClientId());
        bodyMap.add("client_secret", accessTknConfig.getClientSecret());
        logger.info("Getting the access token during the application startup:{}", bodyMap);
        accessToken = RestUtils.getFreshAccessToken(restTemplate, accessTknConfig);

    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Deprecated
    private ResponseEntity<String> callForAccessToken() {
        return restTemplate.exchange(accessTknConfig.getAccesstokenURI(), HttpMethod.POST,
                new HttpEntity(bodyMap, headers), String.class);
    }

    @Deprecated
    public String getFreshAccessToken() {
        ResponseEntity<String> accessTokenResponse = callForAccessToken();
        if (HttpStatus.OK == accessTokenResponse.getStatusCode()) {
            /*Gson json = new Gson();
            return json.fromJson(accessTokenResponse.getBody(), HashMap.class).get(ACCESSTOKEN_KEY).toString();*/
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(accessTokenResponse.getBody(), new TypeReference<Map<String, String>>() {
                });
                return map.get(RestUtils.ACCESSTOKEN_KEY).toString();
            } catch (IOException ex) {
                throw new BulkConnectorException("Exception while... fetching the Fresh Access Token", ex);
            }

        }
        return null;
    }

    public String getRefreshAccessToken() {
        this.accessToken = RestUtils.getFreshAccessToken(restTemplate, accessTknConfig);
        return accessToken;
    }

}
