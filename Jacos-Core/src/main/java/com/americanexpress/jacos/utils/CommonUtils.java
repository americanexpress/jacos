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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Objects;


/**
 * @since 1.0
 */
public class CommonUtils {

    private CommonUtils() {

    }

    /**
     * <p>Parse CSV line and returns the each element in output list.</p>
     * <p>
     * It uses open-csv to process record
     *
     * @param csvLine line to be converted to csv elements
     * @return list of elements
     */
    public static String[] csvDecode(String csvLine) {
        StringReader reader = new StringReader(csvLine);
        try (CSVReader csvReader = new CSVReader(reader);) {
            return csvReader.readNext();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse csv", e);
        }
    }


    /**
     * This method write all csvLine elements in the CSV format and returns as string.
     * <p>
     * THis method uses apache commons-csv for create csv line from string.
     *
     * @param csvLine elements for csv string
     * @return string representation of csvLine elements
     */

    public static String csvEncode(String... csvLine) {
        String output = "";
        try (StringWriter sw = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(sw)) {
            csvWriter.writeNext(csvLine, true);
            csvWriter.flush();
            output = sw.toString();
            int i = output.lastIndexOf('\n');
            if (i > -1) {
                output = output.substring(0, i);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse csv", e);
        }
        return output;
    }


    public static String removeNewLine(String content) {
        return Objects.isNull(content) ? content : content.replaceAll("(\n)|(\r)", " ");
    }

}
