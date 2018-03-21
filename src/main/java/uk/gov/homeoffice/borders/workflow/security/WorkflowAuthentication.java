package uk.gov.homeoffice.borders.workflow.security;

import org.camunda.bpm.engine.impl.identity.Authentication;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
//        super(user.getUsername(), user.getRoles().stream()
//                .map(Role::getName).collect(toList()), new ArrayList<>());
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
