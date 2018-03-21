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

    public Page<Task> tasks(@NotNull User user, Pageable pageable) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .processVariableValueNotEquals("type", "notifications")
                .initializeFormKeys();
        taskQuery = applyUserFilters(user, taskQuery);
        Long totalResults = taskQuery.count();
        log.info("Total results for query '{}'", totalResults);

        if (pageable.getSort() != null) {
            log.info("Sort defined...applying");
            taskSortExecutor.applySort(taskQuery, pageable.getSort());
        }

        int pageNumber = calculatePageNumber(pageable);

        List<Task> tasks = taskQuery
                .listPage(pageNumber, pageable.getPageSize());

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
        return Team.flatten(user.getTeam()).map(Team::getId).collect(toList());
    }


    public void claimTask(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskService.claim(task.getId(), user.getEmail());
        log.info("Task '{}' claimed", taskId);
    }

    public void completeTask(User user, String taskId, CompleteTaskDto completeTaskDto) {
        Task task = validateTaskCanBeCompletedByUser(user, getTask(user, taskId));

        if (completeTaskDto == null) {
            taskService.complete(task.getId());
            log.info("task completed without any variables");
        } else {
            VariableMap variables = VariableValueDto.toMap(completeTaskDto.getVariables(), processEngine, objectMapper);
            taskService.complete(task.getId(), variables);
        }
    }

    public void completeTaskWithForm(User user, String taskId, CompleteTaskDto completeTaskDto) {
        Task task = validateTaskCanBeCompletedByUser(user, getTask(user, taskId));
        VariableMap variables = VariableValueDto.toMap(completeTaskDto.getVariables(), processEngine, objectMapper);
        formService.submitTaskForm(task.getId(), variables);
    }

    private Task validateTaskCanBeCompletedByUser(User user, Task task) {
        if (!task.getAssignee().equalsIgnoreCase(user.getEmail())) {
            throw new ForbiddenException("Task cannot be completed by user");
        }
        return task;
    }

    public Task getTask(User user, String taskId) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .initializeFormKeys()
                .taskId(taskId);
        Task task = applyUserFilters(user, taskQuery).singleResult();
        taskExistsCheck(taskId, task);
        return task;
    }


    public void unclaim(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskService.setAssignee(task.getId(), null);
        log.info("Task '{}' unclaimed");
    }

    public Page<Task> query(User user, TaskQueryDto queryDto, Pageable pageable) {
        TaskQuery taskQuery = queryDto.toQuery(processEngine);

        taskQuery = applyUserFilters(user, taskQuery);

        long totalResults = taskQuery.count();

        int pageNumber = calculatePageNumber(pageable);

        List<Task> tasks = taskQuery
                .listPage(pageNumber, pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalResults);
    }

    public VariableMap getVariables(User user, String taskId) {
        Task task = getTask(user, taskId);
        taskExistsCheck(taskId, task);
        return taskService.getVariablesTyped(task.getId(), true);
    }

    private void taskExistsCheck(String taskId, Task task) {
        if (task == null) {
            throw new ResourceNotFound(String.format("Task with id %s does not exist.", taskId));
        }
    }
}
