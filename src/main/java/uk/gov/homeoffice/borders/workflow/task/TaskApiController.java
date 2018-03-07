package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;
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
@SuppressWarnings("unchecked")
public class TaskApiController {

    private TaskApplicationService applicationService;
    private TaskDtoResourceAssembler taskDtoResourceAssembler;
    private PagedResourcesAssembler pagedResourcesAssembler;
    private RestApiUserExtractor restApiUserExtractor;

    @GetMapping
    public PagedResources<TaskDtoResource> tasks(Pageable pageable) {
        Page<Task> page = applicationService.tasks(restApiUserExtractor.toUser(), pageable);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);
    }


    @PostMapping("/api/engine/tasks/{taskId}/_claim")
    public ResponseEntity<?> claim(@PathVariable String taskId) {
        applicationService.claimTask(restApiUserExtractor.toUser(), taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/engine/tasks/{taskId}/_complete")
    public ResponseEntity<?> complete(@PathVariable String taskId) {
        applicationService.completeTask(restApiUserExtractor.toUser(), taskId);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/api/engine/tasks/{taskId}/_unclaim")
    public ResponseEntity<?> unclaim(@PathVariable String taskId) {
        applicationService.unclaim(restApiUserExtractor.toUser(), taskId);
        return ResponseEntity.ok().build();
    }



}
