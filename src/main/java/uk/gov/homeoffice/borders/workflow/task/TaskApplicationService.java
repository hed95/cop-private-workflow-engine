package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private TaskService taskService;
    private TaskSortExecutor taskSortExecutor;
    private ProcessEngine processEngine;
    private FormService formService;
    private ObjectMapper objectMapper;

    /**
     * Returns paged result of tasks
     *
     * @param user     user that is returned from active session look up
     * @param assignedToMeOnly used to indicate to just return tasks assigned to user
     * @param pageable page object
     *
     * @return paged result
     */
    public Page<Task> tasks(@NotNull User user, Boolean assignedToMeOnly, Boolean unassignedOnly,Pageable pageable) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .processVariableValueNotEquals("type", "notifications")
                .initializeFormKeys();

        if (assignedToMeOnly) {
            taskQuery = taskQuery.taskAssignee(user.getEmail());
        } else if (unassignedOnly) {
            taskQuery = taskQuery.taskCandidateGroupIn(user.getTeams().stream().map(Team::getTeamCode).collect(toList()))
                    .taskUnassigned();
        } else {
            taskQuery = applyUserFilters(user, taskQuery);
        }

        Long totalResults = taskQuery.count();
        log.info("Total results for query '{}'", totalResults);

        if (pageable.getSort() != null) {
            log.info("Sort defined...applying");
            taskSortExecutor.applySort(taskQuery, pageable.getSort());
        }
        List<Task> tasks = taskQuery
                .listPage(calculatePageNumber(pageable), pageable.getPageSize());

        return new PageImpl<>(tasks, new PageRequest(pageable.getPageNumber(), pageable.getPageSize()), totalResults);
    }

    public int calculatePageNumber(Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            return 0;
        }
        return pageable.getPageNumber() * pageable.getPageSize();
    }


    private TaskQuery applyUserFilters(@NotNull User user, TaskQuery taskQuery) {
        return taskQuery.or()
                .taskAssignee(user.getEmail())
                .taskCandidateGroupIn(resolveCandidateGroups(user))
                .includeAssignedTasks()
                .endOr();
    }


    private List<String> resolveCandidateGroups(User user) {
        return user.getTeams().stream().map(Team::getTeamCode).collect(toList());
    }

    /**
     * Claims ownership of the task
     *
     * @param user
     * @param taskId
     */
    public void claimTask(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskService.claim(task.getId(), user.getEmail());
        log.info("Task '{}' claimed", taskId);
    }

    /**
     * Completes the task. Once complete it cannot be re-opened. You will need
     * to create another task with the same data in order to do a 'reopen'
     *
     * @param user
     * @param taskId
     * @param completeTaskDto
     */
    public void completeTask(User user, String taskId, CompleteTaskDto completeTaskDto) {
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

    /**
     * Complete task with form data
     *
     * @param user
     * @param taskId
     * @param completeTaskDto
     */
    public void completeTaskWithForm(User user, String taskId, CompleteTaskDto completeTaskDto) {
        Task task = getTask(user, taskId);
        validateTaskCanBeCompletedByUser(user, task);
        VariableMap variables = VariableValueDto.toMap(completeTaskDto.getVariables(), processEngine, objectMapper);
        formService.submitTaskForm(task.getId(), variables);
    }

    private void validateTaskCanBeCompletedByUser(User user, Task task) {
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
    public Task getTask(User user, String taskId) {
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
    public void unclaim(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskService.setAssignee(task.getId(), null);
        log.info("Task '{}' unclaimed");
    }

    /**
     * Perform a task query based on a set of criterias
     *
     * @param user
     * @param queryDto
     * @param pageable
     * @return
     * @see TaskQueryDto
     * @see User
     */
    public Page<Task> query(User user, TaskQueryDto queryDto, Pageable pageable) {
        TaskQuery taskQuery = queryDto.toQuery(processEngine);
        taskQuery = applyUserFilters(user, taskQuery);
        long totalResults = taskQuery.count();

        int pageNumber = calculatePageNumber(pageable);

        List<Task> tasks = taskQuery
                .listPage(pageNumber, pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalResults);
    }

    /**
     * Return variables associated with task
     *
     * @param user
     * @param taskId
     * @return
     */
    public VariableMap getVariables(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskExistsCheck(taskId, task);
        return taskService.getVariablesTyped(task.getId(), false);
    }

    private void taskExistsCheck(String taskId, Task task) {
        if (task == null) {
            throw new ResourceNotFound(String.format("Task with id %s does not exist.", taskId));
        }
    }

    public List<IdentityLink> getIdentityLinksForTask(String id) {
        return taskService.getIdentityLinksForTask(id);
    }


    public TasksCountDto taskCounts(User user) {
        TasksCountDto tasksCountDto = new TasksCountDto();

        List<String> teamCodes = user.getTeams().stream().map(Team::getTeamCode).collect(toList());

        Long tasksAssignedToUser = taskService.createTaskQuery().taskAssignee(user.getEmail()).count();
        tasksCountDto.setTasksAssignedToUser(tasksAssignedToUser);


        Long unassignedTasks = taskService.createTaskQuery().taskCandidateGroupIn(teamCodes)
                .taskUnassigned().count();

        tasksCountDto.setTasksUnassigned(unassignedTasks);

        Long totalTasksAllocatedToTeam = taskService.createTaskQuery().taskCandidateGroupIn(teamCodes)
                    .includeAssignedTasks()
                    .count();

        tasksCountDto.setTotalTasksAllocatedToTeam(totalTasksAllocatedToTeam);

        return tasksCountDto;
    }
}
