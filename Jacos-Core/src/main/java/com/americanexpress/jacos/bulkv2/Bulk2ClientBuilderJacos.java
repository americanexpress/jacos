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

package com.americanexpress.jacos.bulkv2;

import com.americanexpress.jacos.commons.RefreshTokenCallback;
import com.americanexpress.jacos.utils.Json;
import endolabs.salesforce.bulkv2.AccessToken;
import endolabs.salesforce.bulkv2.BulkRequestException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * +
 * This class builds the Bulk2ClientJacos using the builder pattern
 *
 * @param <T> Refresh Token callback help
 */
public class Bulk2ClientBuilderJacos<T> {

    private static final Logger log = LoggerFactory.getLogger(Bulk2ClientBuilderJacos.class);

    private static final String TOKEN_REQUEST_ENDPOINT = "https://login.salesforce.com/services/oauth2/token";

    private static final String TOKEN_REQUEST_ENDPOINT_SANDBOX = "https://test.salesforce.com/services/oauth2/token";

    private boolean useSandbox;

    private boolean useProxy;

    private String apiVersion = "v41.0";

    private Proxy proxy;

    private boolean bypassProxyAuthentication;

    private Authenticator proxyAuthenticator;

    private Supplier<AccessToken> accessTokenSupplier;

    private Integer timeout;

    private RefreshTokenCallback<T> refreshTokenCallBack;
    private T configBean;

    /**
     * +
     * Sets state/behaviour while building the client.
     *
     * @param configBean
     * @return
     */
    public Bulk2ClientBuilderJacos setCallbackConfigBean(T configBean) {
        this.configBean = configBean;
        return this;
    }

    /**
     * +
     * Uses password get the access token
     *
     * @param consumerKey
     * @param consumerSecret
     * @param username
     * @param password
     * @return
     */
    public Bulk2ClientBuilderJacos withPassword(String consumerKey, String consumerSecret, String username, String password) {
        this.accessTokenSupplier = () -> this.getAccessTokenUsingPassword(consumerKey, consumerSecret, username, password);

        return this;
    }

    /**
     * +
     * Sets the new access token
     *
     * @param token
     * @param instanceUrl
     * @return
     */
    public Bulk2ClientBuilderJacos withSessionId(String token, String instanceUrl) {
        this.accessTokenSupplier = () -> {
            AccessToken accessToken = new AccessToken();
            accessToken.setAccessToken(token);
            accessToken.setInstanceUrl(instanceUrl);
            return accessToken;
        };

        return this;
    }

    /**
     * +
     * Updates the refresh toke with latest refresh token
     *
     * @param refreshTokenCallBack
     * @return
     */
    public Bulk2ClientBuilderJacos setRefreshTokenCallback(RefreshTokenCallback<T> refreshTokenCallBack) {
        this.refreshTokenCallBack = refreshTokenCallBack;
        return this;
    }

    /**
     * +
     * Builds with provided API versions
     *
     * @param apiVersion
     * @return
     */
    public Bulk2ClientBuilderJacos useAPIVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }


    public Bulk2ClientBuilderJacos useSandbox() {
        this.useSandbox = true;
        return this;
    }

    /**
     * +
     * If proxy is enabled , use this method to build the client
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     */
    public Bulk2ClientBuilderJacos useProxy(String host, int port, String username, String password) {
        this.useProxy = true;
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        if (username == null || password == null) {
            bypassProxyAuthentication = true;
        } else {
            proxyAuthenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
        }

        return this;
    }


    public Bulk2ClientBuilderJacos useTimeout(Integer timeout) {

        this.timeout = timeout;
        return this;
    }

    /**
     * +
     * Call this method at the end to build the Bulk2ClientJacos
     *
     * @return
     * @throws IOException
     */
    public Bulk2ClientJacos build()
            throws IOException {
        AccessToken token = accessTokenSupplier.get();

        OkHttpClient client = useProxy ? (bypassProxyAuthentication ?
                new OkHttpClient.Builder()
                        .proxy(proxy)
                        .addInterceptor(authorizationInterceptor(token.getAccessToken()))
                        .addInterceptor(httpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY))
                        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                        .readTimeout(timeout, TimeUnit.MILLISECONDS)
                        .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                        .build() :
                new OkHttpClient.Builder()
                        .proxy(proxy)
                        .proxyAuthenticator(proxyAuthenticator)
                        .addInterceptor(authorizationInterceptor(token.getAccessToken()))
                        .addInterceptor(httpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY))
                        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                        .readTimeout(timeout, TimeUnit.MILLISECONDS)
                        .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                        .build()) :
                new OkHttpClient.Builder()
                        .addInterceptor(authorizationInterceptor(token.getAccessToken()))
                        .addInterceptor(httpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY))
                        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                        .readTimeout(timeout, TimeUnit.MILLISECONDS)
                        .writeTimeout(timeout, TimeUnit.MILLISECONDS)

                        .build();

        return new Bulk2ClientJacos(new RestRequesterJacos(client), token.getInstanceUrl(), apiVersion, configBean, refreshTokenCallBack);
    }

    private AccessToken getAccessTokenUsingPassword(String consumerKey, String consumerSecret, String username, String password) {
        String endpoint = useSandbox ? TOKEN_REQUEST_ENDPOINT_SANDBOX : TOKEN_REQUEST_ENDPOINT;
        HttpUrl authorizeUrl = HttpUrl.parse(endpoint).newBuilder().build();

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("client_id", consumerKey)
                .add("client_secret", consumerSecret)
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(authorizeUrl)
                .post(requestBody)
                .build();


        OkHttpClient client = new OkHttpClient().newBuilder()
                // .addInterceptor(new SigningInterceptor(consumer))
                .addInterceptor(httpLoggingInterceptor(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            return Json.decode(responseBody.string(), AccessToken.class);
        } catch (IOException e) {
            throw new BulkRequestException(e);
        }
    }

    private Interceptor authorizationInterceptor(String token) {
        return chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(request);
        };
    }

    private HttpLoggingInterceptor httpLoggingInterceptor(HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> log.info(message));
        logging.setLevel(level);
        return logging;
    }
}
