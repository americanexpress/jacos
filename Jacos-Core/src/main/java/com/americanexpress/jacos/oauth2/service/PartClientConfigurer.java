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


import com.americanexpress.jacos.oauth2.configuration.OAuthGetAccessTokenConfig;
import com.americanexpress.jacos.part.PartClient;
import com.americanexpress.jacos.part.PartClientBuilder;
import com.americanexpress.jacos.utils.RestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * Spring based configurer to initialize part client. This is a autowired component if you use a spring based application project only.
 * <p>
 * For pure java/non-spring based application , This Class should not to be used. Use of low level API's like PartClientBuilder instead of this class
 */
@Service
@ConfigurationProperties("jacos.sfdc")
public class PartClientConfigurer {
    private static final Logger logger = LogManager.getLogger(PartClientConfigurer.class);

    private PartClient partClient;
    @Autowired
    private AccessTknForRefreshTkn accessTknForRefreshTkn;

    @Autowired
    private OAuthGetAccessTokenConfig accessTknConfig;

    @Value("instance_url")
    private String instanceURL;

    @Value("api_version")
    private String apiVersion;

    private Map<String, String> requestHeaders;

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getInstanceURL() {
        return instanceURL;
    }

    public void setInstanceURL(String instanceURL) {
        this.instanceURL = instanceURL;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }


    @PostConstruct
    public void init() {
        try {
            RestUtils.ConfigBean configBean = new RestUtils.ConfigBean(accessTknConfig.getGrantType(), accessTknConfig.getRefreshToken(), accessTknConfig.getClientId(), accessTknConfig.getClientSecret(), accessTknConfig.getContentType(), accessTknConfig.getAccesstokenURI());
            if (accessTknForRefreshTkn.isProxyEnabled()) {
                partClient = new PartClientBuilder<RestUtils.ConfigBean>()
                        .setCallbackConfigBean(configBean)
                        .setRefreshTokenCallback((restTemplate, configBean1) -> {
                            return RestUtils.getFreshAccessToken(restTemplate, configBean);
                        })
                        .useAPIVersion(apiVersion)
                        .withSessionId(accessTknForRefreshTkn.getAccessToken(), instanceURL)
                        .useProxy(accessTknForRefreshTkn.getHost(), accessTknForRefreshTkn.getPort(), accessTknForRefreshTkn.getUserName(), accessTknForRefreshTkn.getPassword())
                        .build();
            } else {
                partClient = new PartClientBuilder<RestUtils.ConfigBean>()
                        .setCallbackConfigBean(configBean)
                        .setRefreshTokenCallback((restTemplate, configBean1) -> {
                            return RestUtils.getFreshAccessToken(restTemplate, configBean);
                        })
                        .useAPIVersion(apiVersion)
                        .withSessionId(accessTknForRefreshTkn.getAccessToken(), instanceURL)
                        .build();
            }
        } catch (
                IOException e) {
            throw new RuntimeException("Error in Initializing the Part Client...");
        }

        // set the request headers read from the configuration and configure it in part client.
        if (partClient != null && requestHeaders != null) {
            HttpHeaders restHeaders = partClient.getRestHeaders();

            if (restHeaders == null)
                restHeaders = new HttpHeaders();

            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                restHeaders.add(entry.getKey(), entry.getValue());
            }
            partClient.setRestHeaders(restHeaders);
        }

    }


    public PartClient getPartClient() {
        return partClient;
    }

    public void setPartClient(PartClient partClient) {
        this.partClient = partClient;
    }

    public PartClient refreshPartClient() {
        try {
            RestUtils.ConfigBean configBean = new RestUtils.ConfigBean(accessTknConfig.getGrantType(), accessTknConfig.getRefreshToken(), accessTknConfig.getClientId(), accessTknConfig.getClientSecret(), accessTknConfig.getContentType(), accessTknConfig.getAccesstokenURI());

            this.partClient = new PartClientBuilder<RestUtils.ConfigBean>()
                    .setCallbackConfigBean(configBean)
                    .setRefreshTokenCallback((restTemplate, configBean1) -> {
                        return RestUtils.getFreshAccessToken(restTemplate, configBean);
                    })
                    .useAPIVersion(apiVersion)
                    .withSessionId(accessTknForRefreshTkn.getAccessToken(), instanceURL)
                    .useProxy(accessTknForRefreshTkn.getHost(), accessTknForRefreshTkn.getPort(), accessTknForRefreshTkn.getUserName(), accessTknForRefreshTkn.getPassword())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Error in Refreshing the Part Client ...");
        }
        return this.partClient;
    }


}