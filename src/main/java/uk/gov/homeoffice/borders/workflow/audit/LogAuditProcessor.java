package uk.gov.homeoffice.borders.workflow.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LogAuditProcessor implements AuditProcessor {

    private ObjectMapper objectMapper;

    @Override
    public void handleAudit(AuditEventListener.AuditEvent auditEvent) throws Exception {
        String json = objectMapper.writeValueAsString(auditEvent);
        log.debug("Audit event from engine: '{}'", json);
    }
}
