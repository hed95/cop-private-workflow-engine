package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingCaseAuditEventListener implements CaseAuditEventListener {

    public void handle(CaseAudit caseAudit) {
            log.info("{} invoked by {} and with args {}", caseAudit.getType(),
                caseAudit.getPlatformUser().getEmail(),
                caseAudit.getArgs());
    }
}
