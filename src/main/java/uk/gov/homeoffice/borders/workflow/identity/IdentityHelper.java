package uk.gov.homeoffice.borders.workflow.identity;


import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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

    /**
     * Sets a process variable 'candidateGroupsForCurrentUser'. You can use this in the following:
     * <p>#{identityHelper.setCandidateGroupsForCurrentUser(execution)}</p>
     * @param delegateExecution
     */
    public void setCandidateGroupsForCurrentUser(DelegateExecution delegateExecution) {
        WorkflowAuthentication currentAuthentication = (WorkflowAuthentication) identityService.getCurrentAuthentication();
        String candidateGroups =  ofNullable(currentAuthentication).map((authentication) -> {
            List<String> teamCodes = authentication.getUser().getTeams().stream().map(Team::getTeamCode).collect(toList());
            return String.join(",", teamCodes);
        }).orElse("");

        delegateExecution.setVariable("candidateGroupsForCurrentUser", candidateGroups);
    }
}
