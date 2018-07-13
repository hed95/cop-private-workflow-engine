package uk.gov.homeoffice.borders.workflow;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Used to log all events within the workflow engine.
 */
@Slf4j
@Component
@CamundaSelector
public class AuditEventListener extends ReactorExecutionListener {

    private ObjectMapper objectMapper;
    private IdentityService identityService;

    @Autowired
    public AuditEventListener(ObjectMapper objectMapper, IdentityService identityService) {
        this.objectMapper = objectMapper;
        this.identityService = identityService;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Authentication currentAuthentication = identityService.getCurrentAuthentication();
        AuditEvent auditEvent = AuditEvent.createFrom(execution, currentAuthentication);
        String json = objectMapper.writeValueAsString(auditEvent);
        log.debug("Audit event: '{}'", json);
    }


    @Data
    public static class AuditEvent {
        private String processInstanceId;
        private String processBusinessKey;
        private String processDefinitionId;
        private String parentId;
        private String currentActivityId;
        private String currentActivityName;
        private String activityInstanceId;
        private String parentActivityInstanceId;
        private String currentTransitionId;
        private String tenantId;
        private String executedBy;
        private Date date;

        static AuditEvent createFrom(DelegateExecution execution, Authentication currentAuthentication) {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setProcessInstanceId(execution.getProcessInstanceId());
            auditEvent.setProcessBusinessKey(execution.getProcessBusinessKey());
            auditEvent.setProcessDefinitionId(execution.getProcessDefinitionId());
            auditEvent.setParentId(execution.getParentId());
            auditEvent.setCurrentActivityId(execution.getCurrentActivityId());
            auditEvent.setCurrentActivityName(execution.getCurrentActivityName());
            auditEvent.setCurrentTransitionId(execution.getCurrentTransitionId());
            auditEvent.setTenantId(execution.getTenantId());
            if (currentAuthentication != null) {
                auditEvent.setExecutedBy(currentAuthentication.getUserId());
            }
            auditEvent.setDate(new Date());
            return auditEvent;
        }
    }
}
