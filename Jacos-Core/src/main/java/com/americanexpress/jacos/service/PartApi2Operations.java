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

import com.americanexpress.jacos.bulkv2.request.OperationInfo;
import com.americanexpress.jacos.exception.PartApiException;
import com.americanexpress.jacos.model.CompositeAPIResponse;
import com.americanexpress.jacos.oauth2.service.PartClientConfigurer;
import com.americanexpress.jacos.part.CompositeUpdateReqFileSerializer;
import com.americanexpress.jacos.part.PartClient;
import com.americanexpress.jacos.utils.CommonUtils;
import com.americanexpress.jacos.utils.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class PartApi2Operations {
    private static final Logger LOGGER = LogManager.getLogger(PartApi2Operations.class);

    @Autowired
    private PartClientConfigurer partClientConfigurer;

    public String createAndSubmitJob(OperationInfo operationInfo, String jsonContent) throws PartApiException {
        LOGGER.info("PartApi Op begin, OperationInfo:{}", operationInfo);
        PartClient partClient = partClientConfigurer.getPartClient();
        String response = partClient.modifyObject(operationInfo.getObjectType(), operationInfo.getOperationType(), jsonContent, operationInfo.getExternalIdField());
        LOGGER.info("PartApi Op end, SF response:{}", CommonUtils.removeNewLine(response));
        return response;
    }

    public String performSFOperation(OperationInfo operationInfo, String jsonContent) throws PartApiException {
        return createAndSubmitJob(operationInfo, jsonContent);
    }

    /**
     * <p>It read data from <code>inputFile</code> and perform the part operation using salesforce composite API.
     * <br>
     * This method splits input file into multiple chunks then each send it to SF using the composite API.
     * The number of records per chunk is decided based on the SF API limit (200) and can be changed using the CompositeUpdateReqFileSerializer while init.
     * Each record SF operation status is written in outputFilePath.
     * </p>
     *
     *
     * <p>Salesforce composite API request eg:
     * <p>
     * "{\n" +
     * "   \"allOrNone\" : false,\n" +
     * "   \"records\" : [{\n" +
     * "      \"attributes\" : {\"type\" : \"Referral__c\"},\n" +
     * "      \"id\" : \"a822i000000004p\",\n" +
     * "      \"Account_Owner_Email__c\" : \"abcdef@gmail.com\"\n" +
     * "   }]" +
     * "}"
     *
     * </p>
     *
     * <p> Salesforce composite API request eg: [{
     * "id" : "a822i000000004pAAA",
     * "success" : true,
     * "errors" : [ ]
     * } ]
     * 13:03:18.048 [main] INFO  PartApi2Operations - [ {
     * "id" : "a822i000000004pAAA",
     * "success" : true,
     * "errors" : [ ]
     * }]
     *
     * </p>
     *
     * @param operationInfo  contains the operation details
     * @param inputFile      input file to process
     * @param outputFilePath Path to write each operation output
     * @throws PartApiException if there are any exception during processing the file
     * @see <a href="https://developer.salesforce.com/blogs/tech-pubs/2017/01/simplify-your-api-code-with-new-composite-resources.html">SF composite API</a>
     */
    public void performSFOperation(OperationInfo operationInfo,
                                   String inputFile, String outputFilePath) throws PartApiException {

        LOGGER.info("PerformSFOperation Begin: OperationInfo:{} input file:{}, output file:{}", operationInfo, inputFile, outputFilePath);

        Set<String> fileUniqueIds = new HashSet<>();
        try (FileOutputStream fos = new FileOutputStream(outputFilePath, true)) {

            Iterator<String> serializer = new CompositeUpdateReqFileSerializer(inputFile, operationInfo.getObjectType());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            TypeReference<CompositeAPIResponse[]> typeReference = new TypeReference<CompositeAPIResponse[]>() {
            };
            while (serializer.hasNext()) {
                String objs = serializer.next();
                LOGGER.debug("Performing multiupdate:{}", objs);
                String response = createAndSubmitJob(operationInfo, objs);
                if (null != response) {
                    /**
                     * Convert Json string to CompositeAPIResponse, extract id,status and error fields then write to file
                     */
                    CompositeAPIResponse[] responsesObj = Json.decode(response, typeReference);
                    Arrays.stream(responsesObj)
                            //Extract fields from response object
                            .map(responseObj ->
                                    new String[]{
                                            responseObj.getId()
                                            , Boolean.toString(responseObj.getSuccess())
                                            //If no errors then place null
                                            , responseObj.getErrors() == null || responseObj.getErrors().length == 0 ? null : Arrays.toString(responseObj.getErrors())})
                            //Avoid dup ids in file
                            .filter(sArr -> StringUtils.isBlank(sArr[0]) || fileUniqueIds.add(sArr[0]))
                            //Convert string to csv string line
                            .map(s -> CommonUtils.csvEncode(s))
                            //Write to file
                            .forEach(s -> {
                                try {
                                    bw.write(s);
                                    bw.newLine();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                }

                bw.flush();

            }

        } catch (IOException e) {
            LOGGER.error(e);
            throw new PartApiException("Failed to process file:" + outputFilePath, e);
        }
        LOGGER.info("PerformSFOperation End: OperationInfo:{} input file:{}, output file:{}", operationInfo, inputFile, outputFilePath);
    }


}
