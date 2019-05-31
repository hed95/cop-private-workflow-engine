package uk.gov.homeoffice.borders.workflow.identity;


import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
public class IdentityHelper {

    @Autowired
    private IdentityService identityService;

    public String candidateGroupsForCurrentUser() {
        WorkflowAuthentication currentAuthentication = (WorkflowAuthentication) identityService.getCurrentAuthentication();

        return ofNullable(currentAuthentication).map((authentication) -> {
            List<String> teamCodes = authentication.getUser().getTeams().stream().map(Team::getCode).collect(toList());
            return String.join(",", teamCodes);
        }).orElse("");
    }
}
