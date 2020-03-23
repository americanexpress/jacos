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

import com.americanexpress.jacos.commons.RefreshTokenCallback;
import com.americanexpress.jacos.exception.RestTemplateErrorHandler;
import endolabs.salesforce.bulkv2.AccessToken;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @param <T> - refreshtoken callback helper
 * <p>
 * Partclient builder is used to build the partclient using builder pattern
 */
public class PartClientBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(PartClientBuilder.class);


    private boolean useSandbox;

    private boolean useProxy;

    private Supplier<AccessToken> accessTokenSupplier;


    private RestTemplate restTemplate = new RestTemplate();
    private String apiVersion;

    private RefreshTokenCallback<T> refreshTokenCallBack;
    private T configBean;


    public PartClientBuilder setCallbackConfigBean(T configBean) {
        this.configBean = configBean;
        return this;
    }

    public PartClientBuilder withSessionId(String token, String instanceUrl) {
        this.accessTokenSupplier = () -> {
            AccessToken accessToken = new AccessToken();
            accessToken.setAccessToken(token);
            accessToken.setInstanceUrl(instanceUrl);
            return accessToken;
        };

        return this;
    }

    public PartClientBuilder setRefreshTokenCallback(RefreshTokenCallback<T> refreshTokenCallBack) {
        this.refreshTokenCallBack = refreshTokenCallBack;
        return this;
    }


    public PartClientBuilder useSandbox() {
        this.useSandbox = true;
        return this;
    }

    public PartClientBuilder useAPIVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public PartClientBuilder useProxy(String host, int port, String username, String password) {
        this.useProxy = true;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        boolean isProxyAuthRequired = username != null && password != null;
        if (isProxyAuthRequired) {
            credsProvider.setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(username, password));
        }

        HttpHost myProxy = new HttpHost(host, port);
        HttpClient client = isProxyAuthRequired ? HttpClientBuilder.create().
                setProxy(myProxy). //build();
                setDefaultCredentialsProvider(credsProvider).disableCookieManagement().build()
                : HttpClientBuilder.create().
                setProxy(myProxy).build();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
        this.restTemplate = new RestTemplate(clientHttpRequestFactory);
        this.restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        return this;
    }


    public PartClient build()
            throws IOException {
        AccessToken token = accessTokenSupplier.get();
        HttpHeaders restHeaders = new HttpHeaders();
        restHeaders.setContentType(MediaType.APPLICATION_JSON);
        restHeaders.add("X-PrettyPrint", "1");
        restHeaders.add("Authorization", "Bearer " + token.getAccessToken());

        //restHeaders.entrySet().stream().map()
        return new PartClient<T>(token.getInstanceUrl(), restTemplate, restHeaders, configBean, refreshTokenCallBack, apiVersion);
    }

}
