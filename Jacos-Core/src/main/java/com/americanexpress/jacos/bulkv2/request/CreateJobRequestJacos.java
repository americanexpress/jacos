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

package com.americanexpress.jacos.bulkv2.request;

import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;
import com.fasterxml.jackson.annotation.JsonIgnore;
import endolabs.salesforce.bulkv2.type.ColumnDelimiterEnum;
import endolabs.salesforce.bulkv2.type.ContentTypeEnum;
import endolabs.salesforce.bulkv2.type.LineEndingEnum;

import java.io.File;

public class CreateJobRequestJacos {

    private final ColumnDelimiterEnum columnDelimiter;

    private final ContentTypeEnum contentType;

    private final String externalIdFieldName;

    private final LineEndingEnum lineEnding;

    private final String object;

    private final OperationEnumJacos operation;

    @JsonIgnore
    private final String content;

    @JsonIgnore
    private final File contentFile;

    private CreateJobRequestJacos(Builder builder) {
        this.columnDelimiter = builder.columnDelimiter;
        this.contentType = builder.contentType;
        this.externalIdFieldName = builder.externalIdFieldName;
        this.lineEnding = builder.lineEnding;
        this.object = builder.object;
        this.operation = builder.operation;
        this.content = builder.content;
        this.contentFile = builder.contentFile;
    }

    public ColumnDelimiterEnum getColumnDelimiter() {
        return columnDelimiter;
    }

    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public String getExternalIdFieldName() {
        return externalIdFieldName;
    }

    public LineEndingEnum getLineEnding() {
        return lineEnding;
    }

    public String getObject() {
        return object;
    }

    public OperationEnumJacos getOperation() {
        return operation;
    }

    public String getContent() {
        return content;
    }

    public File getContentFile() {
        return contentFile;
    }

    public static class Builder {

        private String object;

        private OperationEnumJacos operation;

        private ColumnDelimiterEnum columnDelimiter;

        private ContentTypeEnum contentType;

        private String externalIdFieldName;

        private LineEndingEnum lineEnding;

        private String content;

        private File contentFile;

        public Builder(String object, OperationEnumJacos operation) {
            this.object = object;
            this.operation = operation;
            this.contentType = ContentTypeEnum.CSV;
        }

        public Builder withColumnDelimiter(ColumnDelimiterEnum columnDelimiter) {
            this.columnDelimiter = columnDelimiter;
            return this;
        }

        public Builder withContentType(ContentTypeEnum contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withExternalIdFieldName(String externalIdFieldName) {
            this.externalIdFieldName = externalIdFieldName;
            return this;
        }

        public Builder withLineEnding(LineEndingEnum lineEnding) {
            this.lineEnding = lineEnding;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withContent(File file) {
            this.contentFile = contentFile;
            return this;
        }

        public CreateJobRequestJacos build() {
            return new CreateJobRequestJacos(this);
        }
    }
}
