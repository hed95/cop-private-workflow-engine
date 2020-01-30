package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.event.CustomHistoryEventHandler;
import uk.gov.homeoffice.borders.workflow.event.CustomHistoryEventLevel;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class HistoryConfiguration extends AbstractCamundaConfiguration {


    @Override
    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("Configuring history");

        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("03:00");
        processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("05:00");

        List<HistoryLevel> customHistoryLevels = processEngineConfiguration.getCustomHistoryLevels();
        if (customHistoryLevels == null) {
            customHistoryLevels = new ArrayList<>();
            processEngineConfiguration.setCustomHistoryLevels(customHistoryLevels);
        }
        customHistoryLevels.add(CustomHistoryEventLevel.getInstance());
        processEngineConfiguration.setCustomHistoryLevels(customHistoryLevels);
        processEngineConfiguration.setHistoryEventHandler(new CompositeDbHistoryEventHandler(new CustomHistoryEventHandler()));
        log.info("History configured");
    }

}
