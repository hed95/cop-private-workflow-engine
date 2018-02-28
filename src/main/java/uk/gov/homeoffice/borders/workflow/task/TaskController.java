package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.homeoffice.borders.workflow.task.TasksApiPaths.ROOT_PATH;

@RestController
@RequestMapping(path = ROOT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskController {

    private TaskApplicationService applicationService;
    private IdentityService identityService;

    @GetMapping
    public List<TaskDto> tasks() {
        return applicationService
                .tasks(toUser()).stream()
                .map(TaskDto::fromEntity)
                .collect(toList());
    }

    private User toUser() {
        WorkflowAuthentication currentAuthentication = (WorkflowAuthentication) identityService.getCurrentAuthentication();
        if (currentAuthentication == null) {
            throw new ForbiddenException("No current authentication detected.");
        }
        return currentAuthentication.getUser();
    }


}
