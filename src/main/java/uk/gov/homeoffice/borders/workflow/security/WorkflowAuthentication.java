package uk.gov.homeoffice.borders.workflow.security;

import org.camunda.bpm.engine.impl.identity.Authentication;
import uk.gov.homeoffice.borders.workflow.identity.Group;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorkflowAuthentication extends Authentication {

    private User user;

    public WorkflowAuthentication() {
        super();
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> groupIds) {
        super(authenticatedUserId, groupIds, null);
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> authenticatedGroupIds, List<String> authenticatedTenantIds) {
        super(authenticatedUserId, authenticatedGroupIds, authenticatedTenantIds);
    }

    public WorkflowAuthentication(User user) {
        super(user.getUsername(), user.getGroups().stream()
                .map(Group::getName).collect(Collectors.toList()), new ArrayList<>());
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
