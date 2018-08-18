package uk.gov.homeoffice.borders.workflow.security;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static java.util.Optional.ofNullable;


@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessEngineIdentityFilter extends OncePerRequestFilter {

    private static final String SERVICE_ROLE = "service_role";
    private IdentityService identityService;
    private KeycloakSecurityContext keycloakSecurityContext;
    private AntPathMatcher antPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        long serviceRoleCount = keycloakSecurityContext.getToken().getRealmAccess().getRoles().stream()
                .filter(r -> r.equalsIgnoreCase(SERVICE_ROLE)).count();

        String userId = keycloakSecurityContext.getToken().getEmail();
        if (serviceRoleCount == 0) {
            WorkflowAuthentication workflowAuthentication =
                    ofNullable(toUser(userId)).map(WorkflowAuthentication::new).
                            orElse(new WorkflowAuthentication(userId, new ArrayList<>()));
            identityService.setAuthentication(workflowAuthentication);
        } else {
            log.debug("Service account user...'{}'", userId);
            identityService.setAuthentication(new WorkflowAuthentication(userId, new ArrayList<>()));
        }
        try {
            chain.doFilter(request, response);
        } finally {
            identityService.clearAuthentication();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConfig.NO_AUTH_URLS.stream()
                .anyMatch(path -> antPathMatcher.match(path, request.getServletPath()));
    }

    private ShiftUser toUser(String userId) {
        return (ShiftUser) identityService.createUserQuery().userId(userId).singleResult();
    }

}
