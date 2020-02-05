package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

@Component
@Slf4j
public class CaseAuthorizationEvaluator {

    public boolean isAuthorized(CaseDetail caseDetail, PlatformUser platformUser) {
        return true;
    }
}
