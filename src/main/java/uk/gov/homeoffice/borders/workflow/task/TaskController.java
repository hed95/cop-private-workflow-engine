package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.homeoffice.borders.workflow.task.TasksApiPaths.ROOT_PATH;

@RestController
@RequestMapping(path = ROOT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE )
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskController {

    private KeycloakSecurityContext securityContext;
    private AccessToken accessToken;

    @GetMapping
    public List<TaskDto> tasks(Principal principal) {
        log.info("id: {}", principal.getName());
        log.info("AccessToken: " + securityContext.getTokenString());
        log.info("User: {} / {}", accessToken.getPreferredUsername(), accessToken.getName());
        log.info("Principal: {}", principal.getName());
        log.info("Roles: {}", securityContext.getToken().getRealmAccess().getRoles());

        return new ArrayList<>();
    }

}
