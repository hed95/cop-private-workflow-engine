package uk.gov.homeoffice.borders.workflow.config;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.event.FormObjectSplitter;
import uk.gov.homeoffice.borders.workflow.event.FormToS3PutRequestGenerator;
import uk.gov.homeoffice.borders.workflow.event.FormVariableS3PersistListener;


@Component
@Slf4j
public class HistoryConfiguration extends AbstractCamundaConfiguration {

    @Value("#{environment.BUCKET_NAME_PREFIX}")
    private String productPrefix;

    private final AmazonS3 amazonS3;
    private final FormToS3PutRequestGenerator formToS3PutRequestGenerator;

    public HistoryConfiguration(AmazonS3 amazonS3, FormToS3PutRequestGenerator formToS3PutRequestGenerator) {
        this.amazonS3 = amazonS3;
        this.formToS3PutRequestGenerator = formToS3PutRequestGenerator;
    }

    @Override
    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("Configuring history");

        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("03:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("05:00");

        processEngineConfiguration.setHistoryEventHandler(
                new CompositeDbHistoryEventHandler(
                        new FormVariableS3PersistListener(processEngineConfiguration.getRuntimeService(),
                                processEngineConfiguration.getRepositoryService(), new FormObjectSplitter(),
                                amazonS3, productPrefix, formToS3PutRequestGenerator)));
        log.info("History configured");
    }

}
