package uk.gov.homeoffice.borders.workflow.security;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication.SERVICE_ROLE;


@Slf4j
@AllArgsConstructor
public class ProcessEngineIdentityFilter extends OncePerRequestFilter {

    private IdentityService identityService;
    private KeycloakSecurityContext keycloakSecurityContext;
    private AntPathMatcher antPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Optional<String> serviceRole = keycloakSecurityContext.getToken()
                .getRealmAccess().getRoles().stream()
                .filter(r -> r.equalsIgnoreCase(SERVICE_ROLE)).findFirst();

        String userId = keycloakSecurityContext.getToken().getEmail();

        WorkflowAuthentication workflowAuthentication = serviceRole.map(role -> {
            log.debug("Service account user...'{}'", userId);
            return createServiceRoleAuthentication(userId);
        }).orElseGet(() -> ofNullable(toUser(userId)).map(WorkflowAuthentication::new).
                orElse(new WorkflowAuthentication(userId, new ArrayList<>())));

        identityService.setAuthentication(workflowAuthentication);
        try {
            chain.doFilter(request, response);
        } finally {
            identityService.clearAuthentication();
        }
    }

    private WorkflowAuthentication createServiceRoleAuthentication(String userId) {
        ShiftUser shiftUser = new ShiftUser();
        shiftUser.setEmail(userId);
        Team team = new Team();
        team.setName(SERVICE_ROLE);
        team.setType(SERVICE_ROLE);
        team.setTeamCode(SERVICE_ROLE);
        shiftUser.setTeams(Collections.singletonList(team));
        return new WorkflowAuthentication(shiftUser);
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
