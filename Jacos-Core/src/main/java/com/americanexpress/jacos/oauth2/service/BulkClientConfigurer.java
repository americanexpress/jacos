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


import com.americanexpress.jacos.bulkv2.Bulk2ClientJacos;
import com.americanexpress.jacos.bulkv2.Bulk2ClientBuilderJacos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;

@Service
@ConfigurationProperties("jacos.sfdc")
public class BulkClientConfigurer {
    private static final Logger logger = LogManager.getLogger(BulkClientConfigurer.class);

    private Bulk2ClientJacos bulk2Client;
    @Autowired
    private AccessTknForRefreshTkn accessTknForRefreshTkn;
    @Value("instance_url")
    private String instanceURL;

    @Value("api_version")
    private String apiVersion;

    @Min(value = 1, message = "Chunk size should not be less than 1 MB")
    @Max(value = 100, message = "Chunk size should not be greater than 100 MB")
    private int chunkSizeInMB;

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

    public int getChunkSizeInMB() {
        return chunkSizeInMB;
    }

    public void setChunkSizeInMB(int chunkSizeInMB) {
        this.chunkSizeInMB = chunkSizeInMB;
    }

    @PostConstruct
    public void init() {
        try {
            if (accessTknForRefreshTkn.isProxyEnabled()) {
                bulk2Client = new Bulk2ClientBuilderJacos()
                        .withSessionId(accessTknForRefreshTkn.getAccessToken(), instanceURL)
                        .useProxy(accessTknForRefreshTkn.getHost(), accessTknForRefreshTkn.getPort(),
                                accessTknForRefreshTkn.getUserName(),
                                accessTknForRefreshTkn.getPassword())
                        .useTimeout(accessTknForRefreshTkn.getTimeout())
                        .build();
            } else {
                bulk2Client = new Bulk2ClientBuilderJacos()
                        .withSessionId(accessTknForRefreshTkn.getAccessToken(), instanceURL)
                        .build();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error in Initializing the Bulk2 Client...", e);
        }

    }

    public Bulk2ClientJacos getBulk2Client() {
        return bulk2Client;
    }

    public void setBulk2Client(Bulk2ClientJacos bulk2Client) {
        this.bulk2Client = bulk2Client;
    }

    public Bulk2ClientJacos refreshBulkClient() {
        synchronized (this) {
            accessTknForRefreshTkn.getRefreshAccessToken();
            init();
        }
        return this.bulk2Client;
    }
}
