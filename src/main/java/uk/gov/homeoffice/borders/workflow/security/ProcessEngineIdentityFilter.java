package uk.gov.homeoffice.borders.workflow.security;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;


@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessEngineIdentityFilter extends GenericFilterBean {

    private IdentityService identityService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            throw new ForbiddenException("No active security context set");
        }
        if (context.getAuthentication() instanceof KeycloakAuthenticationToken) {
            final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) context.getAuthentication();
            RefreshableKeycloakSecurityContext keycloakSecurityContext = ((SimpleKeycloakAccount) token.getDetails()).getKeycloakSecurityContext();

            long serviceRoleCount = keycloakSecurityContext.getToken().getRealmAccess().getRoles().stream()
                    .filter(r -> r.equalsIgnoreCase("service_role")).count();

            String userId = keycloakSecurityContext.getToken().getEmail();
            if (serviceRoleCount == 0) {
                ShiftUser user = toUser(userId);
                if (user == null) {
                    log.warn("User '{}' does not have active shift", userId);
                    identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
                } else {
                    log.debug("User '{}' has active shift", user);
                    identityService.setAuthentication(new WorkflowAuthentication(user));
                }
            } else {
                log.debug("Service account user...'{}'", userId);
                identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            identityService.clearAuthentication();
        }

    }

    private ShiftUser toUser(String userId) {
        return (ShiftUser) identityService.createUserQuery().userId(userId).singleResult();
    }

}
