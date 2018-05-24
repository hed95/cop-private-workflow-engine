package uk.gov.homeoffice.borders.workflow.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.ArrayList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityEventListener {

    private IdentityService identityService;

    @EventListener
    public void onSuccessfulAuthentication(InteractiveAuthenticationSuccessEvent successEvent) {
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) successEvent.getSource();
        RefreshableKeycloakSecurityContext keycloakSecurityContext = ((SimpleKeycloakAccount) keycloakAuthenticationToken.getDetails()).getKeycloakSecurityContext();

        long serviceRoleCount = keycloakSecurityContext.getToken().getRealmAccess().getRoles().stream()
                .filter(r -> r.equalsIgnoreCase("service_role")).count();

        String userId = keycloakSecurityContext.getToken().getEmail();
        if (serviceRoleCount == 0) {
            User user = toUser(userId);
            if (user == null) {
                log.warn("User '{}' does not have active shift", userId);
                identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
            } else {
                log.debug("User '{}' has active session", user);
                identityService.setAuthentication(new WorkflowAuthentication(user));
            }
        } else {
            log.debug("Service account user...'{}'", userId);
            identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
        }
    }

    private User toUser(String userId) {
        return (User) identityService.createUserQuery().userId(userId).singleResult();
    }
}
