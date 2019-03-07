package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.IdentityLink;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.homeoffice.borders.workflow.task.TasksApiPaths.TASKS_ROOT_API;

/**
 * REST API for interacting with tasks
 * Tasks are human tasks that are created by a workflow
 */

@RestController
@RequestMapping(path = TASKS_ROOT_API,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApiController {

    private TaskApplicationService applicationService;
    private TaskDtoResourceAssembler taskDtoResourceAssembler;
    private PagedResourcesAssembler<Task> pagedResourcesAssembler;
    private ObjectMapper objectMapper;
    private ProcessApplicationService processApplicationService;

    @GetMapping
    public PagedResources<TaskDtoResource> tasks(TaskCriteria taskCriteria,
                                                 Pageable pageable,
                                                 PlatformUser platformUser) {
        Page<Task> page = applicationService.tasks(platformUser, taskCriteria, pageable);
        List<String> processDefinitionIds = page.getContent().stream().map(Task::getProcessDefinitionId).collect(toList());

        List<ProcessDefinition> definitions = processApplicationService.getDefinitions(processDefinitionIds);
        Map<String, ProcessDefinition> definitionIdMaps = definitions.stream().collect(toMap(ProcessDefinition::getId, v -> v));
        PagedResources<TaskDtoResource> resources = pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);
        resources.forEach(dto -> Optional.ofNullable(definitionIdMaps.get(dto.getTaskDto().getProcessDefinitionId()))
                .ifPresent(d -> dto.setProcessDefinition(ProcessDefinitionDto.fromProcessDefinition(d))));

        return resources;
    }

    @GetMapping("/{taskId}")
    @SuppressWarnings("unchecked")
    public CompletableFuture<TaskDtoResource> task(@PathVariable String taskId,
                                                   @RequestParam(required = false, defaultValue = "false") Boolean includeVariables, PlatformUser user)  {
        Mono<Task> task = Mono
                .fromCallable(() -> applicationService.getTask(user, taskId))
                .subscribeOn(Schedulers.elastic());

        Mono<List<String>> identities = Mono.fromCallable(() -> applicationService.getIdentityLinksForTask(taskId)
                .stream().map(IdentityLink::getGroupId)
                .filter(Objects::nonNull)
                .collect(toList()))
                .subscribeOn(Schedulers.elastic());

        if (includeVariables) {
            Mono<Map<String, VariableValueDto>> variableMap = Mono.fromCallable(() -> applicationService.getVariables(user, taskId))
                    .map(VariableValueDto::fromVariableMap)
                    .subscribeOn(Schedulers.elastic());
            return Mono.zip(Arrays.asList(task, identities, variableMap), (Object[] args) -> {
                TaskDtoResource taskDtoResource = taskDtoResourceAssembler.toResource((Task) args[0]);
                taskDtoResource.setCandidateGroups((List<String>) args[1]);
                taskDtoResource.setVariables((Map<String, VariableValueDto>)args[2]);
                return taskDtoResource;
            }).subscribeOn(Schedulers.elastic()).toFuture();
        }
        return Mono.zip(Arrays.asList(task, identities), (Object[] args) -> {
            TaskDtoResource taskDtoResource = taskDtoResourceAssembler.toResource((Task) args[0]);
            taskDtoResource.setCandidateGroups((List<String>) args[1]);
            return taskDtoResource;
        }).subscribeOn(Schedulers.elastic()).toFuture();

    }

    @GetMapping("/{taskId}/variables")
    public Map<String, VariableValueDto> variables(@PathVariable String taskId, PlatformUser platformUser) {
        VariableMap variables = applicationService.getVariables(platformUser, taskId);
        return VariableValueDto.fromVariableMap(variables);
    }

    @PostMapping
    public PagedResources<TaskDtoResource> query(@RequestBody TaskQueryDto queryDto, Pageable pageable, PlatformUser platformUser) {
        Page<Task> page = applicationService.query(platformUser, queryDto, pageable);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);

    }

    @PostMapping("/{taskId}/_claim")
    public ResponseEntity claim(@PathVariable String taskId, PlatformUser platformUser) {
        applicationService.claimTask(platformUser, taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/_complete")
    public ResponseEntity complete(@PathVariable String taskId, @RequestBody(required = false)
            CompleteTaskDto completeTaskDto, PlatformUser platformUser) {
        applicationService.completeTask(platformUser, taskId, completeTaskDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity completeTask(@PathVariable String taskId, @RequestBody(required = false)
            TaskCompleteDto completeTaskDto) throws Exception {
        log.info("Task completed with variables {}", objectMapper.writeValueAsString(completeTaskDto));
        applicationService.completeTask(taskId, completeTaskDto);
        return ResponseEntity.ok().build();

    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@PathVariable String taskId,
                                 @RequestBody TaskDto taskDto,
                                 PlatformUser platformUser) {
        applicationService.updateTask(taskId, taskDto, platformUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{taskId}/form/_complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity completeWithFrom(@PathVariable String taskId, @RequestBody CompleteTaskDto completeTaskDto,
                                           PlatformUser user) {
        applicationService.completeTaskWithForm(user, taskId, completeTaskDto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/{taskId}/_unclaim")
    public ResponseEntity unclaim(@PathVariable String taskId, PlatformUser platformUser) {
        applicationService.unclaim(platformUser, taskId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/_task-counts")
    public CompletableFuture<TasksCountDto> taskCounts(PlatformUser platformUser) {
        return applicationService.taskCounts(platformUser).toFuture();
    }


}
