package uk.gov.homeoffice.borders.workflow.security;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.homeoffice.borders.workflow.config.CorrelationIdInterceptor.CORRELATION_HEADER_NAME;
import static uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication.SERVICE_ROLE;


@Slf4j
@AllArgsConstructor
public class ProcessEngineIdentityFilter extends OncePerRequestFilter {

    private IdentityService identityService;
    private KeycloakSecurityContext keycloakSecurityContext;
    private AntPathMatcher antPathMatcher;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Optional<String> serviceRole = keycloakSecurityContext.getToken()
                .getRealmAccess()
                .getRoles()
                .stream()
                .filter(r -> r.equalsIgnoreCase(SERVICE_ROLE))
                .findFirst();

        String userId = keycloakSecurityContext.getToken().getEmail();
        configureMDC(userId, request);

        WorkflowAuthentication workflowAuthentication = serviceRole.map(role -> {
            log.debug("Service account user...'{}'", userId);
            return createServiceRoleAuthentication(userId);
        }).orElseGet(() -> ofNullable(toUser(userId, keycloakSecurityContext)).map(WorkflowAuthentication::new).
                orElse(new WorkflowAuthentication(userId, new ArrayList<>())));

        identityService.setAuthentication(workflowAuthentication);
        try {
            chain.doFilter(request, response);
        } finally {
            clearMDC();
            identityService.clearAuthentication();
        }
    }

    private WorkflowAuthentication createServiceRoleAuthentication(String userId) {
        PlatformUser platformUser = new PlatformUser();
        platformUser.setEmail(userId);
        Team team = new Team();
        team.setName(SERVICE_ROLE);
        team.setType(SERVICE_ROLE);
        team.setCode(SERVICE_ROLE);
        platformUser.setTeams(Collections.singletonList(team));
        platformUser.setRoles(Collections.singletonList(SERVICE_ROLE));
        return new WorkflowAuthentication(platformUser);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConfig.NO_AUTH_URLS.stream()
                .anyMatch(path -> antPathMatcher.match(path, request.getServletPath()));
    }

    private PlatformUser toUser(String userId, KeycloakSecurityContext keycloakSecurityContext) {
        PlatformUser platformUser = (PlatformUser) identityService.createUserQuery().userId(userId).singleResult();
        if (platformUser != null) {
            ArrayList<String> roles = new ArrayList<>(keycloakSecurityContext.getToken().getRealmAccess().getRoles());
            platformUser.setRoles(roles);
            if (platformUser.getShiftDetails() != null) {
                platformUser.getShiftDetails().setRoles(roles);
            }
        }
        return platformUser;
    }

    private void configureMDC(final String userId, final HttpServletRequest request) {
        MDC.put("userId", userId);
        MDC.put("requestPath", request.getServletPath());
        MDC.put("correlationId", request.getHeader(CORRELATION_HEADER_NAME));
    }

    private void clearMDC() {
        MDC.remove("userId");
        MDC.remove("requestPath");
        MDC.remove("correlationId");
    }

}
