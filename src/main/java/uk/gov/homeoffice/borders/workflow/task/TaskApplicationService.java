package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.digitalpatterns.camunda.encryption.ProcessDefinitionEncryptionParser;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.spin.Spin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uk.gov.homeoffice.borders.workflow.PageHelper;
import uk.gov.homeoffice.borders.workflow.exception.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;
import uk.gov.homeoffice.borders.workflow.exception.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private static final String NOTIFICATIONS = "notifications";
    private TaskService taskService;
    private TaskSortExecutor taskSortExecutor;
    private ProcessEngine processEngine;
    private FormService formService;
    private ObjectMapper objectMapper;
    private JacksonJsonDataFormat formatter;
    private RuntimeService runtimeService;
    private ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor;
    private ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor;
    private ProcessDefinitionEncryptionParser processDefinitionEncryptionParser;
    private RepositoryService repositoryService;

    private static final PageHelper PAGE_HELPER = new PageHelper();

    /**
     * Returns paged result of tasks
     *
     * @param user     user that is returned from active session look up
     * @param pageable page object
     * @return paged result
     */
    public Page<Task> tasks(@NotNull PlatformUser user, TaskCriteria taskCriteria, Pageable pageable) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .processVariableValueNotEquals("type", NOTIFICATIONS)
                .initializeFormKeys();
        if (taskCriteria == null) {
            taskQuery = applyUserFilters(user, taskQuery);
        } else {
            taskQuery = createQuery(user, taskCriteria, taskQuery);
        }
        long totalResults = taskQuery.count();
        log.info("Total results for query '{}'", totalResults);

        if (pageable.getSort() != null) {
            taskSortExecutor.applySort(taskQuery, pageable.getSort());
        }
        List<Task> tasks = taskQuery
                .listPage(PAGE_HELPER.calculatePageNumber(pageable), pageable.getPageSize());
        return new PageImpl<>(tasks, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), totalResults);
    }

    private TaskQuery createQuery(@NotNull PlatformUser user, TaskCriteria taskCriteria, TaskQuery taskQuery) {
        taskCriteria.apply(taskQuery);
        if (taskCriteria.getAssignedToMeOnly()) {
            taskQuery.or()
                    .taskAssignee(user.getEmail())
                    .taskCandidateUser(user.getEmail()).endOr();
        } else {
            List<String> teamCodes = resolveCandidateGroups(user);
            if (taskCriteria.getUnassignedOnly()) {
                taskQuery.taskCandidateGroupIn(teamCodes)
                        .taskUnassigned();
            } else if (taskCriteria.getTeamOnly()) {
                taskQuery.taskCandidateGroupIn(teamCodes)
                        .includeAssignedTasks();
            } else {
                applyUserFilters(user, taskQuery);
            }
        }

        return taskQuery;
    }


    private TaskQuery applyUserFilters(@NotNull PlatformUser user, TaskQuery taskQuery) {
        return taskQuery.or()
                .taskCandidateUser(user.getEmail())
                .taskAssignee(user.getEmail())
                .taskCandidateGroupIn(resolveCandidateGroups(user))
                .includeAssignedTasks()
                .endOr();
    }


    private List<String> resolveCandidateGroups(PlatformUser user) {
        return user.getTeams().stream().map(Team::getCode).collect(toList());
    }

    /**
     * Claims ownership of the task
     *
     * @param user
     * @param taskId
     */
    void claimTask(@NotNull PlatformUser user, String taskId) {
        Task task = getTask(user, taskId);
        if (task.getAssignee() != null && !task.getAssignee().equalsIgnoreCase(user.getEmail())) {
            log.info("Task id '{}' was assigned to '{}'...now being reassigned to '{}'",
                    task.getId(),
                    task.getAssignee(),
                    user.getEmail());
            taskService.setAssignee(taskId, null);
        }
        taskService.claim(task.getId(), user.getEmail());
        log.info("Task '{}' claimed by '{}'", taskId, user.getEmail());
    }

    /**
     * Completes the task. Once complete it cannot be re-opened. You will need
     * to create another task with the same data in order to do a 'reopen'
     *
     * @param user
     * @param taskId
     * @param completeTaskDto
     */
    void completeTask(@NotNull PlatformUser user, String taskId, CompleteTaskDto completeTaskDto) {
        Task task = getTask(user, taskId);
        validateTaskCanBeCompletedByUser(user, task);

        if (completeTaskDto == null) {
            taskService.complete(task.getId());
            log.info("task completed without any variables");
        } else {
            VariableMap variables = VariableValueDto.toMap(completeTaskDto.getVariables(), processEngine, objectMapper);
            taskService.complete(task.getId(), variables);
        }
    }

    void completeTask(String taskId, TaskCompleteDto completeTaskDto) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResourceNotFound(String.format("%s cannot be found", taskId));
        }
        if (task.getAssignee() == null) {
            throw new InternalWorkflowException("No assignee for task " + task.getId());
        }


        Map<String, Object> variables = new HashMap<>();
        if (hasEncryption(task)) {
            variables.put(completeTaskDto.getVariableName(),
                    processInstanceSpinVariableEncryptor.encrypt(completeTaskDto.getData()));
        } else {
            Spin<?> spinObject = Spin.S(completeTaskDto.getData(), formatter);
            variables.put(completeTaskDto.getVariableName(), spinObject);
        }
        runtimeService.setVariables(task.getProcessInstanceId(), variables);
        taskService.complete(task.getId());
        log.info("task completed by {}", task.getAssignee());
    }


    /**
     * Complete task with form data
     *
     * @param user
     * @param taskId
     * @param completeTaskDto
     */
    void completeTaskWithForm(@NotNull PlatformUser user, String taskId, CompleteTaskDto completeTaskDto) {
        Task task = getTask(user, taskId);
        validateTaskCanBeCompletedByUser(user, task);

        VariableMap variables;
        if (hasEncryption(task)) {
            variables = new VariableMapImpl();
            completeTaskDto.getVariables().keySet().forEach(variableName ->
                    variables.putValue(variableName,
                            processInstanceSpinVariableEncryptor.encrypt(completeTaskDto
                                    .getVariables().get(variableName))));
        } else {
            variables = VariableValueDto.toMap(completeTaskDto.getVariables(), processEngine, objectMapper);
        }

        formService.submitTaskForm(task.getId(), variables);
    }

    private boolean hasEncryption(Task task) {
        return processDefinitionEncryptionParser.shouldEncrypt(repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult().getKey(), "encryptVariables");
    }

    private void validateTaskCanBeCompletedByUser(PlatformUser user, Task task) {
        if (!task.getAssignee().equalsIgnoreCase(user.getEmail())) {
            throw new ForbiddenException("Task cannot be completed by user");
        }
    }

    /**
     * Get a single task
     *
     * @param user
     * @param taskId
     * @return
     */
    Task getTask(@NotNull PlatformUser user, String taskId) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .initializeFormKeys()
                .taskId(taskId);
        Task task = applyUserFilters(user, taskQuery).singleResult();
        taskExistsCheck(taskId, task);
        return task;
    }

    /**
     * Return task back into the pool of work that belongs to a candidate group
     *
     * @param user
     * @param taskId
     */
    void unclaim(@NotNull PlatformUser user, String taskId) {
        Task task = getTask(user, taskId);
        taskService.setAssignee(task.getId(), null);
        log.info("Task '{}' unclaimed", taskId);
    }

    /**
     * Perform a task query based on a set of criteria
     *
     * @param user
     * @param queryDto
     * @param pageable
     * @return
     * @see TaskQueryDto
     * @see PlatformUser
     */
    public Page<Task> query(@NotNull PlatformUser user, TaskQueryDto queryDto, Pageable pageable) {
        TaskQuery taskQuery = queryDto.toQuery(processEngine);
        taskQuery = applyUserFilters(user, taskQuery);
        long totalResults = taskQuery.count();

        int pageNumber = PAGE_HELPER.calculatePageNumber(pageable);

        List<Task> tasks = taskQuery
                .listPage(pageNumber, pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalResults);
    }

    /**ProcessApplicationServiceSpec
     * Return variables associated with task
     *
     * @param user
     * @param taskId
     * @return
     */
    VariableMap getVariables(@NotNull PlatformUser user, String taskId) {
        Task task = getTask(user, taskId);
        taskExistsCheck(taskId, task);
        if (hasEncryption(task)) {
            return processInstanceSpinVariableDecryptor.decrypt(taskService.getVariables(taskId));
        } else {
            return taskService.getVariablesTyped(task.getId(), false);
        }
    }

    private void taskExistsCheck(String taskId, Task task) {
        if (task == null) {
            throw new ResourceNotFound(String.format("Task with id %s does not exist.", taskId));
        }
    }

    List<IdentityLink> getIdentityLinksForTask(String id) {
        return taskService.getIdentityLinksForTask(id);
    }


    Mono<TasksCountDto> taskCounts(PlatformUser user) {

        List<String> teamCodes = resolveCandidateGroups(user);

        Mono<Long> assignedToUser = Mono.fromCallable(() -> taskService.createTaskQuery()
                .processVariableValueNotEquals("type", NOTIFICATIONS)
                .or()
                .taskAssignee(user.getEmail())
                .taskCandidateUser(user.getEmail())
                .endOr()
                .count()).subscribeOn(Schedulers.elastic());

        Mono<Long> unassignedTasks = Mono.fromCallable(() -> taskService.createTaskQuery()
                .taskCandidateGroupIn(teamCodes)
                .processVariableValueNotEquals("type", NOTIFICATIONS)
                .taskUnassigned().count())
                .subscribeOn(Schedulers.elastic());

        Mono<Long> tasksAssignedToTeams = Mono.fromCallable(() -> taskService.createTaskQuery()
                .taskCandidateGroupIn(teamCodes)
                .processVariableValueNotEquals("type", NOTIFICATIONS)
                .includeAssignedTasks()
                .count()).subscribeOn(Schedulers.elastic());

        return Mono.zip(Arrays.asList(assignedToUser, unassignedTasks, tasksAssignedToTeams), (Object[] args) -> {
            TasksCountDto tasksCountDto = new TasksCountDto();
            tasksCountDto.setTasksAssignedToUser((Long) args[0]);
            tasksCountDto.setTasksUnassigned((Long) args[1]);
            tasksCountDto.setTotalTasksAllocatedToTeam((Long) args[2]);
            return tasksCountDto;
        }).doOnError((Throwable e) -> log.error("Failed to get task count", e))
                .onErrorReturn(new TasksCountDto())
                .subscribeOn(Schedulers.elastic());

    }

    void updateTask(String taskId, TaskDto taskDto, PlatformUser user) {
        log.info("User {} has requested to update task {}", user.getEmail(), taskId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResourceNotFound(String.format("Task %s does not exist", taskId));
        }
        if (taskDto.getDue() != null && taskDto.getDue() != task.getDueDate()) {
            task.setDueDate(taskDto.getDue());
        }
        taskService.saveTask(task);
    }
}
