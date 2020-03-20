package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.event.FormObjectSplitter;
import uk.gov.homeoffice.borders.workflow.event.FormVariableS3PersistListener;


@Component
@Slf4j
public class HistoryConfiguration extends AbstractCamundaConfiguration {


    @Override
    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("Configuring history");

        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("03:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("05:00");

        processEngineConfiguration.setHistoryEventHandler(
                new CompositeDbHistoryEventHandler(
                        new FormVariableS3PersistListener(processEngineConfiguration.getRuntimeService(),
                                processEngineConfiguration.getRepositoryService(), new FormObjectSplitter())));
        log.info("History configured");
    }

}
