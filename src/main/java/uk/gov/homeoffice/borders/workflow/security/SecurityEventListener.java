package uk.gov.homeoffice.borders.workflow.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityEventListener {

    private IdentityService identityService;

    @EventListener
    public void onSuccessfulAuthentication(InteractiveAuthenticationSuccessEvent successEvent) {

        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) successEvent.getSource();

        List<String> groupIds = getGroupsOfUser(keycloakAuthenticationToken);
        List<String> tenantIds = getTenantsOfUser(keycloakAuthenticationToken);

        identityService.setAuthentication(getUserId(keycloakAuthenticationToken), groupIds, tenantIds);

    }

    private String getUserId(KeycloakAuthenticationToken keycloakAuthenticationToken) {
        return (String) keycloakAuthenticationToken.getPrincipal();
    }

    private List<String> getTenantsOfUser(KeycloakAuthenticationToken keycloakAuthenticationToken) {
        return Collections.emptyList();
    }

    private List<String> getGroupsOfUser(KeycloakAuthenticationToken keycloakAuthenticationToken) {
        return Collections.emptyList();
    }
}
