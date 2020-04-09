package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

@Component
@Slf4j
public class CaseReIndexer {

    private final RestHighLevelClient elasticsearchClient;
    private final  AmazonS3 amazonS3;
    private final AWSConfig awsConfig;

    public static final ActionListener<BulkResponse> DEFAULT_LISTENER = new ActionListener<>() {
        @Override
        public void onResponse(BulkResponse bulkItemResponses) {
            log.info("Bulk index completed '{}'", bulkItemResponses.getItems().length);
        }

        @Override
        public void onFailure(Exception e) {
            log.error("Failed to perform bulk index '{}'", e.getMessage());
        }
    };

    public CaseReIndexer(RestHighLevelClient elasticsearchClient, AmazonS3 amazonS3, AWSConfig awsConfig) {
        this.elasticsearchClient = elasticsearchClient;
        this.amazonS3 = amazonS3;
        this.awsConfig = awsConfig;
    }


    @Async
    public void reindex(String caseId, ActionListener<BulkResponse> actionListener) {
        ObjectListing objectListing = amazonS3.listObjects(awsConfig.getCaseBucketName(),
                format("%s/", caseId));
        BulkRequest bulkRequest = new BulkRequest();
        String indexKey = caseId.split("-")[1];
        objectListing.getObjectSummaries().stream().map(
                summary -> {
                    S3Object object = amazonS3.getObject(awsConfig.getCaseBucketName(), summary.getKey());
                    try {
                        String asJsonString = IOUtils.toString(object.getObjectContent(),
                                StandardCharsets.UTF_8);

                        return new IndexRequest(indexKey).id(object.getKey())
                                .source(asJsonString, XContentType.JSON);

                    } catch (IOException e) {
                        log.error("Failed to get data for '{}'", summary.getKey());
                        return null;
                    }
                }
        ).filter(Objects::nonNull).forEach(bulkRequest::add);

        Optional<ActionListener<BulkResponse>> listener =
                Optional.ofNullable(actionListener);

        elasticsearchClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT.toBuilder()
                .addHeader("Content-Type", "application/json").build(),
                    listener.orElseGet(() -> DEFAULT_LISTENER)
                );




    }
}
