package uk.gov.homeoffice.borders.workflow.security;

import org.camunda.bpm.engine.impl.identity.Authentication;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class WorkflowAuthentication extends Authentication {
    public static final String SERVICE_ROLE = "service_role";

    private PlatformUser user;

    public WorkflowAuthentication() {
        super();
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> groupIds) {
        super(authenticatedUserId, groupIds, null);
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> authenticatedGroupIds, List<String> authenticatedTenantIds) {
        super(authenticatedUserId, authenticatedGroupIds, authenticatedTenantIds);
    }

    public WorkflowAuthentication(PlatformUser user) {
        super(user.getEmail(), user.getTeams().stream().map(Team::getTeamCode).collect(toList()), new ArrayList<>());
        this.user = user;
    }

    public boolean isServiceRole() {
        return this.getGroupIds().stream().filter(SERVICE_ROLE::equalsIgnoreCase).count() == 1;
    }

    public PlatformUser getUser() {
        return this.user;
    }

}
