package uk.gov.homeoffice.borders.workflow.audit;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Used to log all events within the workflow engine.
 */
@Slf4j
@CamundaSelector
public class AuditEventListener extends ReactorExecutionListener {

    private IdentityService identityService;
    private List<AuditProcessor> auditProcessors;

    @Autowired
    public AuditEventListener(IdentityService identityService,
                              List<AuditProcessor> auditProcessors) {
        this.identityService = identityService;
        this.auditProcessors = auditProcessors;
    }

    @Override
    public void notify(DelegateExecution execution){
        Authentication currentAuthentication = identityService.getCurrentAuthentication();
        AuditEvent auditEvent = AuditEvent.createFrom(execution, currentAuthentication);
        this.auditProcessors.stream().forEach(processor -> {
            try {
                processor.handleAudit(auditEvent);
            } catch (Exception e) {
                log.error("Failed to handle audit", e);
            }
        });
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
