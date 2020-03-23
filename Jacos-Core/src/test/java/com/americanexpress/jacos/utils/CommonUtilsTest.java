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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CommonUtilsTest {

    @Test
    public void validCSV() throws IOException {
        Assert.assertEquals("\"xyz\",\"lmn\"", CommonUtils.csvEncode("xyz", "lmn"));
    }

    @Test
    public void validCSVWithComma() throws IOException {

        Assert.assertEquals("\"xy\"\"z\",\"lmn\"", CommonUtils.csvEncode("xy\"z", "lmn"));
    }

    @Test
    public void csvWithComma() throws IOException {
        Assert.assertEquals("\"xyz\",\"lm,n\"", CommonUtils.csvEncode("xyz", "lm,n"));
    }

    @Test
    public void csvWithoutQuote() {
        String line = "01,ASU,Arizona";
        String[] result = CommonUtils.csvDecode(line);
        Assert.assertTrue(result != null);
        Assert.assertEquals(result.length, 3);
        Assert.assertEquals(result[0], "01");
        Assert.assertEquals(result[1], "ASU");
        Assert.assertEquals(result[2], "Arizona");
    }

    @Test
    public void csvWithoutQuoteIn() {
        String line = "\"01\",\"ASU\",\"Arizona\"";
        String[] result = CommonUtils.csvDecode(line);
        Assert.assertTrue(result != null);
        Assert.assertEquals(result.length, 3);
        Assert.assertEquals(result[0], "01");
        Assert.assertEquals(result[1], "ASU");
        Assert.assertEquals(result[2], "Arizona");
    }

    @Test
    public void newLnTest() {
        Assert.assertEquals("a b c d", CommonUtils.removeNewLine("a\nb\rc\nd"));
    }
}
