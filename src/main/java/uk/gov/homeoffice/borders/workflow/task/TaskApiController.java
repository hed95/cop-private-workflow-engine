package uk.gov.homeoffice.borders.workflow.task;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.Map;

import static uk.gov.homeoffice.borders.workflow.task.TasksApiPaths.ROOT_PATH;

@RestController
@RequestMapping(path = ROOT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApiController {

    private TaskApplicationService applicationService;
    private TaskDtoResourceAssembler taskDtoResourceAssembler;
    private PagedResourcesAssembler<Task> pagedResourcesAssembler;
    private RestApiUserExtractor restApiUserExtractor;

    @GetMapping
    public PagedResources<TaskDtoResource> tasks(Pageable pageable) {
        Page<Task> page = applicationService.tasks(restApiUserExtractor.toUser(), pageable);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);
    }

    @GetMapping("/{taskId}")
    public TaskDtoResource task(@PathVariable String taskId) {
        Task task = applicationService.getTask(restApiUserExtractor.toUser(), taskId);
        return taskDtoResourceAssembler.toResource(task);
    }

    @GetMapping("/{taskId}/variables")
    public Map<String, VariableValueDto> variables(@PathVariable String taskId) {
        VariableMap variables = applicationService.getVariables(restApiUserExtractor.toUser(), taskId);
        return VariableValueDto.fromVariableMap(variables);
    }

    @PostMapping
    public PagedResources<TaskDtoResource> query(@RequestBody TaskQueryDto queryDto, Pageable pageable) {
        Page<Task> page = applicationService.query(restApiUserExtractor.toUser(), queryDto, pageable);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);

    }

    @PostMapping("/api/workflow/tasks/{taskId}/_claim")
    public ResponseEntity<?> claim(@PathVariable String taskId) {
        applicationService.claimTask(restApiUserExtractor.toUser(), taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/workflow/tasks/{taskId}/_complete")
    public ResponseEntity<?> complete(@PathVariable String taskId, @RequestBody CompleteTaskDto completeTaskDto) {
        User user = restApiUserExtractor.toUser();
        applicationService.completeTask(user, taskId, completeTaskDto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/api/workflow/tasks/{taskId}/form/_complete")
    public ResponseEntity<?> completeWithFrom(@PathVariable String taskId, @RequestBody CompleteTaskDto completeTaskDto) {
        User user = restApiUserExtractor.toUser();
        applicationService.completeTaskWithForm(user, taskId, completeTaskDto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/api/workflow/tasks/{taskId}/_unclaim")
    public ResponseEntity<?> unclaim(@PathVariable String taskId) {
        applicationService.unclaim(restApiUserExtractor.toUser(), taskId);
        return ResponseEntity.ok().build();
    }


}
