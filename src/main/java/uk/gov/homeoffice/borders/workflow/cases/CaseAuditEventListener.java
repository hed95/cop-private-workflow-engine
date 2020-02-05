package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaseAuditEventListener {


    @EventListener
    public void handle(CaseAudit caseAudit) {
        log.info("'{}' performed '{}' for business key '{}'", caseAudit.getRequestBy().getEmail(),
                caseAudit.getType(),
                caseAudit.getRequestBy().getEmail());
    }
}
