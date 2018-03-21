package uk.gov.homeoffice.borders.workflow.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.User;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityEventListener {

    private IdentityService identityService;

    @EventListener
    public void onSuccessfulAuthentication(InteractiveAuthenticationSuccessEvent successEvent) {
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) successEvent.getSource();
        identityService.setAuthentication(new WorkflowAuthentication(toUser(keycloakAuthenticationToken)));
    }

    private User toUser(KeycloakAuthenticationToken keycloakAuthenticationToken) {
        String userId = ((SimpleKeycloakAccount) keycloakAuthenticationToken.getDetails()).getKeycloakSecurityContext()
                .getToken().getEmail();
        User user = (User) identityService.createUserQuery().userId(userId).singleResult();
        if (user == null) {
            throw new ForbiddenException("No user found from store");
        }
        return user;
    }
}
