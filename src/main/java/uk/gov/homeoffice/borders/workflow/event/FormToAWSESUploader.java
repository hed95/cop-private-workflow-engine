package uk.gov.homeoffice.borders.workflow.event;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Slf4j
public class FormToAWSESUploader {

    private final RestHighLevelClient elasticsearchClient;
    private final RuntimeService runtimeService;


    public FormToAWSESUploader(RestHighLevelClient elasticsearchClient,
                               RuntimeService runtimeService) {
        this.elasticsearchClient = elasticsearchClient;
        this.runtimeService = runtimeService;
    }

    public void upload(String form,
                       String key,
                       HistoricProcessInstance processInstance,
                       String executionId) {


        String indexKey;
        if (processInstance.getBusinessKey() != null && processInstance.getBusinessKey().split("-").length > 3) {
            indexKey = processInstance.getBusinessKey().split("-")[1];
        } else {
            indexKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        IndexRequest indexRequest = new IndexRequest(indexKey).id(key);
        JSONObject object = new JSONObject(form);

        if (!object.has("businessKey")) {
            object.put("businessKey", processInstance.getBusinessKey());
        }

        indexRequest.source(object.toString(), XContentType.JSON);
        try {
            final IndexResponse index = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT.toBuilder()
                    .addHeader("Content-Type", "application/json").build());
            log.info("Document uploaded result response'{}'", index.getResult().getLowercase());
        } catch (IOException e) {
            log.error("Failed to create a document in ES due to '{}'", e.getMessage());
            runtimeService.createIncident(
                    FormVariableS3PersistListener.FAILED_TO_CREATE_ES_RECORD,
                    executionId,
                    format("Failed to create ES document for %s",
                            processInstance.getBusinessKey()),
                    e.getMessage()
            );
        }
    }
}
