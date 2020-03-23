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

package com.americanexpress.jacos.bulkv2;

import com.americanexpress.jacos.bulkv2.request.CreateJobRequestJacos;
import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;
import com.americanexpress.jacos.commons.RefreshTokenCallback;

import endolabs.salesforce.bulkv2.request.CloseOrAbortJobRequest;
import endolabs.salesforce.bulkv2.request.CreateJobRequest;
import endolabs.salesforce.bulkv2.request.GetAllJobsRequest;

import endolabs.salesforce.bulkv2.response.*;
import endolabs.salesforce.bulkv2.type.JobStateEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.function.Consumer;

/**
 * +
 * <p>
 * Its class which is embedded with RestRequesterJacos which is wrapper on top of the http client.
 * With the help this class , you can create a job, upload the data , close , abort ...
 * of refresh token callback can write appropriate lambda logic
 * perform various operations.
 *
 * @param <T> T is required for refresh token callback, refresh token callback requires a helper object so that the implementor
 */
public class Bulk2ClientJacos<T> {

    private static final Logger log = LoggerFactory.getLogger(Bulk2ClientJacos.class);

    private final String API_VERSION;

    private final RestRequesterJacos requester;

    private final String instanceUrl;

    private RefreshTokenCallback<T> refreshTokenCallBack;
    private T configBean;

    public Bulk2ClientJacos(RestRequesterJacos requester, String instanceUrl, String apiVersion, T configBean,
                            RefreshTokenCallback<T> refreshTokenCallBack) {

        this.instanceUrl = instanceUrl;
        this.requester = requester;
        this.API_VERSION = apiVersion;
        this.configBean = configBean;
        this.refreshTokenCallBack = refreshTokenCallBack;
    }

    public CreateJobResponse createJob(String object, OperationEnumJacos operation, String externalIdFieldName) {
        return createJob(object, operation, externalIdFieldName, (request) -> {
        });
    }

    public CreateJobResponse createJob(String object, OperationEnumJacos operation, String externalIdFieldName, Consumer<CreateJobRequestJacos.Builder> requestBuilder) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest");

        CreateJobRequestJacos.Builder builder = new CreateJobRequestJacos.Builder(object, operation);
        if (StringUtils.isNotEmpty(externalIdFieldName)) {
            builder.withExternalIdFieldName(externalIdFieldName);
        }
        requestBuilder.accept(builder);
        return requester.post(url, builder.build(), CreateJobResponse.class);
    }

    public CloseOrAbortJobResponse closeOrAbortJob(String jobId, JobStateEnum state) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId);

        CloseOrAbortJobRequest.Builder builder = new CloseOrAbortJobRequest.Builder(state);

        return requester.patch(url, builder.build(), CloseOrAbortJobResponse.class);
    }

    public void uploadJobData(String jobId, String csvContent) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId + "/batches");

        requester.putCsv(url, csvContent, Void.class);
    }

    public void deleteJob(String jobId) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId);

        requester.delete(url, null, Void.class);
    }

    public GetAllJobsResponse getAllJobs() {
        return getAllJobs(request -> {
        });
    }

    public GetAllJobsResponse getAllJobs(Consumer<GetAllJobsRequest.Builder> requestBuilder) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest");

        GetAllJobsRequest.Builder builder = new GetAllJobsRequest.Builder();
        requestBuilder.accept(builder);

        return requester.get(url, builder.buildParameters(), GetAllJobsResponse.class);
    }

    public GetJobInfoResponse getJobInfo(String jobId) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId);

        return requester.get(url, GetJobInfoResponse.class);
    }

    public Reader getJobSuccessfulRecordResults(String jobId) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId + "/successfulResults/");

        return requester.getCsv(url);
    }

    public Reader getJobFailedRecordResults(String jobId) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId + "/failedResults/");

        return requester.getCsv(url);
    }

    public Reader getJobUnprocessedRecordResults(String jobId) {
        String url = buildUrl("/services/data/vXX.X/jobs/ingest/" + jobId + "/unprocessedrecords/");

        return requester.getCsv(url);
    }

    // alias

    public JobInfo closeJob(String jobId) {
        return closeOrAbortJob(jobId, JobStateEnum.UPLOAD_COMPLETE);
    }

    public JobInfo abortJob(String jobId) {
        return closeOrAbortJob(jobId, JobStateEnum.ABORTED);
    }

    private String buildUrl(String path) {
        boolean hasTrailingSlash = instanceUrl.endsWith("/");

        return instanceUrl + (hasTrailingSlash ? "/" : "") + path.replace("vXX.X", API_VERSION);
    }
}
