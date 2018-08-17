package uk.gov.homeoffice.borders.workflow.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AuditConfig {

    @Bean
    public LogAuditProcessor logAuditProcessor(ObjectMapper objectMapper) {
        return new LogAuditProcessor(objectMapper);
    }

    @Bean
    public AuditEventListener auditEventListener(IdentityService identityService, LogAuditProcessor logAuditProcessor) {
        List<AuditProcessor> auditProcessors = new ArrayList<>();
        auditProcessors.add(logAuditProcessor);
        return new AuditEventListener(identityService, auditProcessors);
    }
}
