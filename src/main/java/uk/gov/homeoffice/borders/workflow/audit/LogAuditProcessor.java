package uk.gov.homeoffice.borders.workflow.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@AllArgsConstructor
public class LogAuditProcessor implements AuditProcessor {

    private ObjectMapper objectMapper;

    @Override
    public void handleAudit(AuditEventListener.AuditEvent auditEvent)  {
        try {
            String json = objectMapper.writeValueAsString(auditEvent);
            log.debug("Audit event from engine: '{}'", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to create JSON event", e);
        }

    }
}
