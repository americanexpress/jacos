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

package com.americanexpress.jacos.service;

import com.americanexpress.jacos.bulkv2.Bulk2ClientJacos;
import com.americanexpress.jacos.bulkv2.request.OperationInfo;
import com.americanexpress.jacos.constants.ApplicationConstants;
import com.americanexpress.jacos.oauth2.service.BulkClientConfigurer;
import endolabs.salesforce.bulkv2.BulkRequestException;
import endolabs.salesforce.bulkv2.response.CreateJobResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BulkApi2Operations {

    private static final Logger logger = LogManager.getLogger(BulkApi2Operations.class);

    private static final Pattern ERROR_PATTERN = Pattern.compile("(?i)INVALID_SESSION_ID|(connect timed out)");

    @Autowired
    private BulkClientConfigurer bulkClientConf;


    /**
     * @param bulk2Client
     * @param operationInfo
     * @param csvContent
     * @param count
     * @return
     */
    public CreateJobResponse createAndSubmitJob(Bulk2ClientJacos bulk2Client, OperationInfo operationInfo, String csvContent, int count) {
        CreateJobResponse createJobResponse = null;
        try {
            createJobResponse = bulk2Client.createJob(operationInfo.getObjectType(), operationInfo.getOperationType(), operationInfo.getExternalIdField());
        } catch (BulkRequestException e) {
            logger.error("Failed to submit job", e);
            if (ERROR_PATTERN.matcher(e.getMessage()).find() && count > 0) {
                //Get the refreshed access token
                return createAndSubmitJob(bulkClientConf.refreshBulkClient(), operationInfo, csvContent, --count);
            } else {
                throw e;
            }
        }
        if (createJobResponse == null) {
            return createJobResponse;
        }
        String jobId = createJobResponse.getId();
        bulk2Client.uploadJobData(jobId, csvContent);
        bulk2Client.closeJob(jobId);
        return createJobResponse;
    }

    public CreateJobResponse performSFOperation(OperationInfo operationInfo, String csvContent) {
        return createAndSubmitJob(bulkClientConf.refreshBulkClient(), operationInfo, csvContent, 3);
    }

    public List<CreateJobResponse> performSFOperationFromFile(OperationInfo operationInfo, String filePath) {

        logger.info("Bulk API Op Begin: operation Info:{} , FilePath:{}", operationInfo, filePath);
        List<CreateJobResponse> jobResponseList = new ArrayList<>();
        File file = new File(filePath);

        try (FileInputStream fis = new FileInputStream(file)) {

            logger.info("Total file size to read (in bytes) : " + fis.available());

            if (fis.available() <= (bulkClientConf.getChunkSizeInMB() * ApplicationConstants.MB)) {

                jobResponseList.add(performSFOperation(operationInfo, readContentFromFile(filePath)));
            } else {
                byte[] buf = new byte[bulkClientConf.getChunkSizeInMB() * ApplicationConstants.MB];
                int byteRead = 0;
                while ((byteRead = fis.read(buf)) > 0) {
                    jobResponseList.add(performSFOperation(operationInfo, new String(buf, StandardCharsets.UTF_8)));
                }
            }

        } catch (IOException e) {
            throw new BulkRequestException("Exception occured while calling bulk v2 rest API: ", e);
        }
        logger.info("Bulk API Op end: response:{}", jobResponseList);
        return jobResponseList;
    }

    private String readContentFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
