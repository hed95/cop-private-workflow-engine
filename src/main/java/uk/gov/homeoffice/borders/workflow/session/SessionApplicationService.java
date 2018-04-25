package uk.gov.homeoffice.borders.workflow.session;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Service responsible for dealing with the internal
 * workflow for creating an active session
 */

@Service
@Slf4j
public class SessionApplicationService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${prest-url}")
    private String prestUrl;
    @Value("${prest-db}")
    private String prestDB;

    /**
     * Initiates a workflow for an active session
     * If a workflow already exists for the session id then it will be deleted
     * and a new instance will be created
     * @param activeSession Active session required
     * @return Process instance...the workflow that has started
     * @see ActiveSession
     * @see ProcessInstance
     */
    public ProcessInstance createSession(@NotNull ActiveSession activeSession) {

        log.info("Starting an active session request");

        ProcessInstance existingSessionForPerson = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(activeSession.getSessionId())
                .singleResult();


        if (existingSessionForPerson != null) {
            restTemplate.delete(String.format("%s/%s/public/activesession?sessionid=%s", prestUrl, prestDB, activeSession.getSessionId()));
            log.info("active session deleted from store.");
            runtimeService.deleteProcessInstance(existingSessionForPerson.getProcessInstanceId(), "remove-active-session");
            log.info("Removed previous workflow session with id '{}'", activeSession.getSessionId());
        }

        setEndTime(activeSession);

        ObjectValue notificationObjectValue =
                Variables.objectValue(activeSession)
                        .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                        .create();

            Map<String, Object> variables = new HashMap<>();
        variables.put("activeSession", notificationObjectValue);
        variables.put("type", "non-notification");

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("activate-session", activeSession.getSessionId(), variables);
        log.info("Active session with id '{}' has started '{}'", activeSession.getSessionId(), processInstance.getProcessInstanceId());
        return processInstance;
    }

    private void setEndTime(ActiveSession activeSession) {
        Integer shiftHours = activeSession.getShiftHours();
        Integer shiftMinutes = activeSession.getShiftMinutes();
        Date startTime = activeSession.getStartTime();
        activeSession.setEndTime(new DateTime(startTime)
                .plusHours(shiftHours)
                .plusMinutes(shiftMinutes)
        .toDate());
    }

    /**
     * Deletes a workflow with the given session id
     * @param sessionId identifies the session that needs to be deleted
     * @param deleteReason This is required
     *
     * @see ProcessInstance
     */
    public void deleteSession(@NotNull String sessionId, @NotNull String deleteReason) {
        ProcessInstance existingSessionForPerson = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(sessionId)
                .singleResult();

        if (existingSessionForPerson == null) {
            log.warn("Process instance does not exist based on session identifier '{}'", sessionId);
            throw new ResourceNotFound("Session does not exist for '" + sessionId + "'");
        }
        restTemplate.delete(String.format("%s/%s/public/activesession?sessionid=%s", prestUrl, prestDB, sessionId));
        log.info("active session deleted from store");
        runtimeService.deleteProcessInstance(existingSessionForPerson.getProcessInstanceId(), deleteReason);
        log.info("Session process deleted '{}'", sessionId);
    }

    /**
     * Returns an active session from the process instance that is running
     * Throws ResourceNotFound exception if the workflow does not exist
     * @param sessionIdentifier Session id required
     * @return ActiveSession
     * @see ActiveSession
     * @throws ResourceNotFound
     */
    public ActiveSession getActiveSession(@NotNull String sessionIdentifier) {
        ProcessInstance session = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(sessionIdentifier)
                .singleResult();

        if (session == null) {
            throw new ResourceNotFound("Active session not found for '" + sessionIdentifier + "'");
        }

        VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(session.getProcessInstanceId())
                .variableName("activeSession").singleResult();

        if (variableInstance != null) {
            return (ActiveSession) variableInstance.getValue();
        }

        throw new ResourceNotFound("Active session could not be located");
    }

}
