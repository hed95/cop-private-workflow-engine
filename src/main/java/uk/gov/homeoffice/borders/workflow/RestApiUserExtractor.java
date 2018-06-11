package uk.gov.homeoffice.borders.workflow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RestApiUserExtractor {

    private IdentityService identityService;

    public ShiftUser toUser() {
        WorkflowAuthentication currentAuthentication = (WorkflowAuthentication) identityService.getCurrentAuthentication();
        if (currentAuthentication == null) {
            throw new ForbiddenException("No current authentication detected.");
        }
        if (currentAuthentication.getUser() == null) {
            throw new ForbiddenException("No active shift detected for user.");
        }
        return currentAuthentication.getUser();
    }

}
