package uk.gov.homeoffice.borders.workflow.cases;

import org.springframework.context.event.EventListener;

public interface CaseAuditEventListener {

    @EventListener
    public void handle(CaseAudit caseAudit);
}
