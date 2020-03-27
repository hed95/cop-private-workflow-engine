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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@Slf4j
@AllArgsConstructor
public class FormVariableS3PersistListener implements HistoryEventHandler {
    protected static final List<String> VARIABLE_EVENT_TYPES = new ArrayList<>();
    private static final ConcurrentHashMap<String, Boolean> S3_SAVE_CHECK = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> S3_PRODUCT = new ConcurrentHashMap<>();

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private FormObjectSplitter formObjectSplitter;
    private String productPrefix;
    private FormToS3Uploader formToS3Uploader;
    public static final String FAILED_TO_CREATE_S3_RECORD = "FAILED_TO_CREATE_S3_RECORD";

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

                Boolean disableExplicitS3Save = S3_SAVE_CHECK.computeIfAbsent(variable.getProcessDefinitionId(), id -> {
                    BpmnModelInstance model = Bpmn.
                            readModelFromStream(this.repositoryService.getProcessModel(id));

                    return model.getModelElementsByType(CamundaProperty.class)
                            .stream()
                            .filter(p -> p.getCamundaName().equalsIgnoreCase("disableExplicitFormDataS3Save"))
                            .findAny()
                            .map(CamundaProperty::getCamundaValue)
                            .map(Boolean::valueOf)
                            .orElse(Boolean.FALSE);
                });


                if (!disableExplicitS3Save) {
                    registerSynchronization(new VariableS3TransactionSynchronisation(historyEvent));
                }
            }
        }
    }

    @AllArgsConstructor
    @Slf4j
    public class VariableS3TransactionSynchronisation extends TransactionSynchronizationAdapter {
        private HistoryEvent historyEvent;

        @Override
        public void afterCompletion(int status) {
            super.afterCompletion(status);
            try {
                HistoricVariableUpdateEventEntity variable = (HistoricVariableUpdateEventEntity) historyEvent;
                String asJson = IOUtils.toString(variable.getByteValue(), "UTF-8");
                List<String> forms = formObjectSplitter.split(asJson);
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(variable.getProcessInstanceId()).singleResult();
                if (!forms.isEmpty()) {
                    String product =
                            productPrefix + "-" + S3_PRODUCT.computeIfAbsent(variable.getProcessDefinitionId(), id -> {
                                BpmnModelInstance model = Bpmn.
                                        readModelFromStream(repositoryService
                                                .getProcessModel(variable.getProcessDefinitionId()));

                                return model.getModelElementsByType(CamundaProperty.class)
                                        .stream()
                                        .filter(p -> p.getCamundaName().equalsIgnoreCase("product"))
                                        .findAny()
                                        .map(CamundaProperty::getCamundaValue)
                                        .orElse("cop-case");

                            });
                    forms.forEach(form ->
                            formToS3Uploader.upload(form, processInstance, variable.getExecutionId(), product));

                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof IOException) {
                    runtimeService.createIncident(
                            FAILED_TO_CREATE_S3_RECORD,
                            historyEvent.getExecutionId(),
                            "Failed to generate JSON from variable",
                            e.getMessage()

                    );
                } else {
                    runtimeService.createIncident(
                            FAILED_TO_CREATE_S3_RECORD,
                            historyEvent.getExecutionId(),
                            "Failed to perform transaction sync",
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
