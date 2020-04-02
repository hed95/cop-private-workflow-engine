package uk.gov.homeoffice.borders.workflow.config;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.event.FormObjectSplitter;
import uk.gov.homeoffice.borders.workflow.event.FormToAWSESUploader;
import uk.gov.homeoffice.borders.workflow.event.FormToS3Uploader;
import uk.gov.homeoffice.borders.workflow.event.FormVariableS3PersistListener;


@Component
@Slf4j
public class HistoryConfiguration extends AbstractCamundaConfiguration {

    @Value("${aws.bucket-name-prefix}")
    private String productPrefix;

    private final AmazonS3 amazonS3;
    private final RestHighLevelClient restHighLevelClient;

    public HistoryConfiguration(AmazonS3 amazonS3, RestHighLevelClient restHighLevelClient) {
        this.amazonS3 = amazonS3;
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("Configuring history");

        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("03:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("05:00");

        final RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();
        processEngineConfiguration.setHistoryEventHandler(
                new CompositeDbHistoryEventHandler(
                        new FormVariableS3PersistListener(runtimeService,
                                processEngineConfiguration.getHistoryService(),
                                processEngineConfiguration.getRepositoryService(),
                                new FormObjectSplitter(),
                                productPrefix, new FormToS3Uploader(runtimeService, amazonS3),
                                new FormToAWSESUploader(restHighLevelClient, runtimeService)
                        )));
        log.info("History configured");
    }

}
