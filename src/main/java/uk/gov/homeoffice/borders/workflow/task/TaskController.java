package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.homeoffice.borders.workflow.task.TasksApiPaths.ROOT_PATH;

@RestController
@RequestMapping(path = ROOT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskController {

    private KeycloakSecurityContext securityContext;
    private AccessToken accessToken;
    private TaskApplicationService applicationService;
    private IdentityService identityService;

    @GetMapping
    public List<TaskDto> tasks(Principal principal) {
        User user = toUser(principal);

        return applicationService
                .tasks(user).stream()
                .map(TaskDto::fromEntity)
                .collect(toList());
    }

    private User toUser(Principal principal) {
        Authentication currentAuthentication = identityService.getCurrentAuthentication();
        if (currentAuthentication == null) {
            throw new ForbiddenException("No current authentication detected.");
        }

        return null;
    }


}
