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

package com.americanexpress.jacos.operations;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractOperation {

    protected String baseServicePath() {
        return "/services/data/vXX.X/sobjects/";
    }

    public String createRelativeUrl(String objectName, String objectId) {
        return formUrlfromBaseUrl(objectName, objectId);
    }

    protected abstract String formUrlfromBaseUrl(String objectName, String objectId);

    public abstract HttpMethod getRequestType();

    public String getResponse(ResponseEntity<String> responseStr) {
        HttpStatus responseStatus = responseStr.getStatusCode();
        return Integer.toString(responseStatus.value());
    }
}
