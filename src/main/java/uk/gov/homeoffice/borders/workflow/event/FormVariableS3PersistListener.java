package uk.gov.homeoffice.borders.workflow.event;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@Slf4j
@AllArgsConstructor
public class FormVariableS3PersistListener implements HistoryEventHandler {
    protected static final List<String> VARIABLE_EVENT_TYPES = new ArrayList<>();

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private FormObjectSplitter formObjectSplitter;

    static {
        VARIABLE_EVENT_TYPES.add(HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEventName());
        VARIABLE_EVENT_TYPES.add(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE.getEventName());
    }

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
        if (historyEvent instanceof HistoricVariableUpdateEventEntity &&
                VARIABLE_EVENT_TYPES.contains(historyEvent.getEventType())) {
            HistoricVariableUpdateEventEntity variable = (HistoricVariableUpdateEventEntity) historyEvent;
            if (variable.getSerializerName().equalsIgnoreCase("json")) {
                registerSynchronization(new VariableS3TransactionSynchronisation(historyEvent,
                        runtimeService, formObjectSplitter));
            }
        }
    }

    @AllArgsConstructor
    public static class VariableS3TransactionSynchronisation extends TransactionSynchronizationAdapter {
        private HistoryEvent historyEvent;
        private RuntimeService runtimeService;
        private FormObjectSplitter formObjectSplitter;

        @Override
        public void afterCompletion(int status) {
            super.afterCompletion(status);
            try {
                HistoricVariableUpdateEventEntity variable = (HistoricVariableUpdateEventEntity) historyEvent;
                String asJson = IOUtils.toString(variable.getByteValue(), "UTF-8");
                List<String> forms = formObjectSplitter.split(asJson);
                if (!forms.isEmpty()) {

                }
            } catch (Exception e) {
                if (e instanceof IOException) {
                    runtimeService.createIncident(
                            "FAILED_TO_CREATE_S3_RECORD",
                            historyEvent.getExecutionId(),
                            "Failed to generate JSON from variable",
                            e.getMessage()

                    );
                } else {
                    runtimeService.createIncident(
                            "FAILED_TO_CREATE_S3_RECORD",
                            historyEvent.getExecutionId(),
                            "Could not upload to S3",
                            e.getMessage()

                    );
                }
            }
        }
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
        historyEvents.forEach(this::handleEvent);
    }
}
