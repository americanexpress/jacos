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

import com.americanexpress.jacos.model.CompositeAPIRequest;
import com.americanexpress.jacos.utils.CommonUtils;
import com.americanexpress.jacos.utils.CompositeAPIReqObjConverter;
import com.americanexpress.jacos.utils.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * <p>
 * An iterator to iterate through the incoming channel and serialize the records in salesforce
 * sobject format.<br>
 * Closeable allows to close the operation at any time.<br>
 * <p>
 * It uses Scanner to read incoming stream File/InputStream, convert to a sobjects bulk record and
 * return it over each iteration. The Size of each bulk record can be controlled using the
 * #bufferSize.<br>
 *
 * <br>
 * <p>
 * As per the current salesforce API spec the current bulk record size is {@value
 * #DEF_BUFFER_SIZE}.</p>
 *
 * @see CompositeAPIRequest
 * @since 1.0
 */
public class CompositeUpdateReqFileSerializer implements Iterator<String>, Closeable {

    public static final int DEF_BUFFER_SIZE = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeUpdateReqFileSerializer.class);


    /**
     * Buffer size to control the batch size per iteration
     */
    final int bufferSize;

    private final String[] header;

    /**
     * Record parser for record to fields
     */

    private final Function<String, String[]> recordParser;


    private final Scanner scanner;

    private final Function<String[], CompositeAPIRequest.Record> compositeAPIReqObjConverter;


    /**
     * Initialize with default buffer size and record parser
     *
     * @param file           file of input datasets
     * @param objectAttrType attribute type of object or object API name
     * @throws IOException if header is not found then throw exception
     */
    public CompositeUpdateReqFileSerializer(String file, String objectAttrType) throws IOException {
        this(new FileInputStream(file), DEF_BUFFER_SIZE, CommonUtils::csvDecode, objectAttrType);
    }

    /**
     * @param file         file of input dataset
     * @param bufferSize   buffersize for each batch
     * @param recordParser record parse to parse each record of the input file
     * @throws IOException if header is not found then throw exception
     */
    public CompositeUpdateReqFileSerializer(String file, int bufferSize, Function<String, String[]> recordParser, String objectAttrType) throws IOException {
        this(new FileInputStream(file), bufferSize, recordParser, objectAttrType);
    }

    /**
     * @param fis          stream of input dataset
     * @param bufferSize   buffersize for each batch
     * @param recordParser record parse to parse each record of the input file
     * @throws IOException if header is not found then throw exception
     */
    public CompositeUpdateReqFileSerializer(InputStream fis, int bufferSize, Function<String, String[]> recordParser,
                                            String objectAttrType) throws IOException {
        this.scanner = new Scanner(fis);
        this.bufferSize = bufferSize;
        this.recordParser = recordParser;
        /**
         * Extract the header from stream. It is expected to be the first line in Scanner.
         */

        if (!scanner.hasNextLine()) {
            throw new IOException("Invalid file. Empty stream");
        }

        header = recordParser.apply(scanner.nextLine());

        compositeAPIReqObjConverter = new CompositeAPIReqObjConverter(header, objectAttrType);


    }

    @Override
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    /**
     * @return json in multi sobjects batch format using the array of CompositeAPIRequest format.
     */
    @Override
    public String next() {


        CompositeAPIRequest request = new CompositeAPIRequest();

        List<CompositeAPIRequest.Record> records = new ArrayList<>();
        while (records.size() < bufferSize && hasNext()) {
            records.add(compositeAPIReqObjConverter.apply(recordParser.apply(scanner.nextLine())));
        }

        LOGGER.debug("Found records from next batch:{}", records);
        LOGGER.info("Total number of records from next batch:{}", records.size());

        request.setRecords(records.toArray(new CompositeAPIRequest.Record[]{}));
        return Json.encode(request);
    }

    @Override
    public void close() {
        scanner.close();
    }
}
