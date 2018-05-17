package uk.gov.homeoffice.borders.workflow.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        if (serviceRoleCount == 0) {
            String userId = keycloakSecurityContext.getToken().getEmail();
            User user = toUser(userId);
            if (user == null) {
                log.warn("User '{}' does not have active shift", userId);
                identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
            } else {
                log.debug("User '{}' has active session", user);
                identityService.setAuthentication(new WorkflowAuthentication(user));
            }
        }
    }

    private User toUser(String userId) {
        return (User) identityService.createUserQuery().userId(userId).singleResult();
    }
}
