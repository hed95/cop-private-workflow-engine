package uk.gov.homeoffice.borders.workflow.session;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SessionApplicationService {

    private RuntimeService runtimeService;

    public ProcessInstance createSession(ActiveSession activeSession) {

        ObjectValue notificationObjectValue =
                Variables.objectValue(activeSession)
                        .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                        .create();

        Map<String, Object> variables = new HashMap<>();
        variables.put("activeSession", notificationObjectValue);
        variables.put("type", "non-notification");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("session", variables);
        log.info("Active session process started '{}'", processInstance.getProcessInstanceId());
        return processInstance;
    }

    public void deleteSession(String sessionId, String deleteReason) {
        runtimeService.deleteProcessInstance(sessionId, deleteReason);
        log.info("Session process deleted '{}'", sessionId);
    }
}
