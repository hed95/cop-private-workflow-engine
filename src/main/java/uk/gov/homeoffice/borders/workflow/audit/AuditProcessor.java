package uk.gov.homeoffice.borders.workflow.audit;


public interface AuditProcessor {

    void handleAudit(AuditEventListener.AuditEvent auditEvent);

}
