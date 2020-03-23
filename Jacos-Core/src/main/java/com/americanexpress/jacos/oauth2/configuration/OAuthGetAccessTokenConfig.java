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

package com.americanexpress.jacos.oauth2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("accessTokenConfig")
@ConfigurationProperties("jacos.sfdc.security.oauth2.client")
public class OAuthGetAccessTokenConfig {

    @Value("access-token-uri")
    private String accesstokenURI;
    @Value("client_id")
    private String clientId;
    @Value("client_secret")
    private String clientSecret;
    @Value("refresh_token")
    private String refreshToken;
    @Value("content_type")
    private String contentType;
    @Value("grant_type")
    private String grantType;

    public String getAccesstokenURI() {
        return accesstokenURI;
    }

    public void setAccesstokenURI(String accesstokenURI) {
        this.accesstokenURI = accesstokenURI;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    @Override
    public String toString() {
        return "OAuthGetAccessTokenConfig{" +
                "accesstokenURI='" + accesstokenURI + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", contentType='" + contentType + '\'' +
                ", grantType='" + grantType + '\'' +
                '}';
    }


}
