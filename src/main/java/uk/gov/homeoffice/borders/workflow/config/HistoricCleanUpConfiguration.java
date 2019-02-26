package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class HistoricCleanUpConfiguration  extends AbstractCamundaConfiguration {

    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("Initialising batch window time for history cleanup");
        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("03:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("05:00");
        log.info("Initialised batch window time for history cleanup");

    }
}
