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

package com.americanexpress.jacos.model;

import java.util.Arrays;

/**
 * The Salesforce composite sobject response schema.
 *
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_sobjects_collections_update.htm">composite_sobjects</a>
 */
public class CompositeAPIResponse {
    private String id;
    private boolean success;
    private ErrorI[] errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorI[] getErrors() {
        return errors;
    }

    public void setErrors(ErrorI[] errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "CompositeAPIResponse{" +
                "id='" + id + '\'' +
                ", success=" + success +
                ", errors=" + Arrays.toString(errors) +
                '}';
    }

    public static class ErrorI {
        private String statusCode;
        private String message;
        private String[] fields;

        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String[] getFields() {
            return fields;
        }

        public void setFields(String[] fields) {
            this.fields = fields;
        }

        @Override
        public String toString() {
            String op = "{" +
                    "statusCode='" + statusCode + '\'' +
                    ", message='" + message + '\'' +
                    ", fields=" + Arrays.toString(fields) +
                    '}';
            return op.replaceAll("\n", "");
        }
    }
}
