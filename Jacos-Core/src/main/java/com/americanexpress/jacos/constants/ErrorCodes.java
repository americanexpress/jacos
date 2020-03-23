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

package com.americanexpress.jacos.constants;

/**
 * Error Code Enum constants
 */
public enum ErrorCodes {
    BULKCONNECTOR_SUCCESS("BULKCONNECTOR PUBLISHED SUCCESSFULLY", "BULKCONNECTOR_1000"),
    BULKCONNECTOR_PARSING_FAILED("BULKCONNECTOR PARSING FAILED", "BULKCONNECTOR_1020"),

    BULKCONNECTOR_SERVICE_DOWN("BULKCONNECTOR  SERVICE RUNTIME ERROR", "BULKCONNECTOR_1030"),
    BULKCONNECTOR_SERVICE_TIMEOUT("BULKCONNECTOR SERVICE TIMED OUT", "BULKCONNECTOR_1031"),
    BULKCONNECTOR_INTERNAL_ERROR("BULKCONNECTOR INTERNAL ERROR", "BULKCONNECTOR_1032"),

    BULKCONNECTOR_HEADER_EMPTY("BULKCONNECTOR HEADER ATTRIBUTE MESSAGE_TYPE IS EMPTY", "BULKCONNECTOR_1040"),
    BULKCONNECTOR_INAVALID_MESSAGE_TYPE("BULKCONNECTOR HEADER ATTRIBUTE MESSAGE_TYPE VALUE IS NOT VALID", "BULKCONNECTOR_1041"),
    BULKCONNECTOR_TOPIC_HEADER_MAPPING_EMPTY("BULKCONNECTOR NO MAPPING FOUND FOR THE HEADER", "BULKCONNECTOR_1042"),

    BULKCONNECTOR_PAYLOAD_EMPTY("BULKCONNECTOR PAYLOAD EMPTY", "BULKCONNECTOR_1050"),

    DEFAULT("BULKCONNECTOR DEFAULT", "BULKCONNECTOR_0000");
    private String message;
    private String code;

    ErrorCodes(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }


    public String getDescription(String customMessage) {
        return "ErrorCode: " + code + " Description: " + message + " Message: " + customMessage;
    }
}
