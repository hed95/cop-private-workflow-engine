package uk.gov.homeoffice.borders.workflow.event;

import groovy.util.logging.Slf4j;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.HistoryService;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.spin.Spin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import javax.crypto.SealedObject;
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
    public static final String FAILED_TO_CREATE_S3_RECORD = "FAILED_TO_CREATE_S3_RECORD";
    public static final String FAILED_TO_CREATE_ES_RECORD = "FAILED_TO_CREATE_ES_RECORD";

    static {
        VARIABLE_EVENT_TYPES.add(HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEventName());
        VARIABLE_EVENT_TYPES.add(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE.getEventName());
    }

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private HistoryService historyService;
    private FormObjectSplitter formObjectSplitter;
    private String productPrefix;
    private FormToS3Uploader formToS3Uploader;
    private FormToAWSESUploader formToAWSESUploader;
    private ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor;


    @Override
    public void handleEvent(HistoryEvent historyEvent) {
        if (historyEvent instanceof HistoricVariableUpdateEventEntity &&
                VARIABLE_EVENT_TYPES.contains(historyEvent.getEventType())) {
            HistoricVariableUpdateEventEntity variable = (HistoricVariableUpdateEventEntity) historyEvent;
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

    @AllArgsConstructor
    public class VariableS3TransactionSynchronisation extends TransactionSynchronizationAdapter {
        private HistoryEvent historyEvent;
        private final Logger log = LoggerFactory.getLogger(VariableS3TransactionSynchronisation.class);
        @Override
        public void afterCompletion(int status) {
            super.afterCompletion(status);
            if (status == STATUS_COMMITTED) {
                try {
                    log.info("Initiating save of form data");
                    HistoricVariableUpdateEventEntity variable = (HistoricVariableUpdateEventEntity) historyEvent;
                    String asJson = null;
                    if ("javax.crypto.SealedObject".equalsIgnoreCase(variable.getTextValue2())) {
                        final SealedObject sealedObject = SerializationUtils.deserialize(variable.getByteValue());
                        final Spin<?> spin = processInstanceSpinVariableDecryptor.decrypt(sealedObject);
                        asJson = spin.toString();
                    } else {
                        if (variable.getSerializerName().equalsIgnoreCase("json")) {
                            asJson = IOUtils.toString(variable.getByteValue(), "UTF-8");
                        }
                    }
                    if (asJson != null) {
                        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                                .processInstanceId(variable.getProcessInstanceId()).singleResult();
                        List<String> forms = formObjectSplitter.split(asJson);
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
                                    {
                                        log.info("Upload form to S3");
                                        String key = formToS3Uploader.upload(form, processInstance, variable.getExecutionId(), product);
                                        if (key != null) {
                                            log.info("Upload form to ES");
                                            formToAWSESUploader.upload(form, key, processInstance, variable.getExecutionId());
                                        }
                                    }
                            );

                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to save to S3 '{}'", e.getMessage());
                    runtimeService.createIncident(
                            FAILED_TO_CREATE_S3_RECORD,
                            historyEvent.getExecutionId(),
                            "Failed perform post transaction activity",
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
