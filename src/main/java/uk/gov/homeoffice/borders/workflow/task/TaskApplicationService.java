package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.Role;
import uk.gov.homeoffice.borders.workflow.identity.User;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private TaskService taskService;
    private TaskSortExecutor taskSortExecutor;
    private TaskChecker taskChecker;
    private ProcessEngine processEngine;

    public Page<Task> tasks(@NotNull User user, Pageable pageable) {
        String taskAssignee = user.getUsername();

        TaskQuery taskQuery = taskService.createTaskQuery()
                .processVariableValueNotEquals("type", "notifications")
                .initializeFormKeys()
                .or();

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            taskQuery = taskQuery.taskCandidateGroupIn(resolveCandidateGroups(user))
                    .includeAssignedTasks();
        }

        taskQuery = taskQuery.taskAssignee(taskAssignee).endOr();
        Long totalResults = taskQuery.count();
        log.info("Total results for query '{}'", totalResults);

        if (pageable.getSort() != null) {
            log.info("Sort defined...applying");
            taskSortExecutor.applySort(taskQuery, pageable.getSort());
        }

        List<Task> tasks = taskQuery.listPage(pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalResults);
    }


    private List<String> resolveCandidateGroups(User user) {
        return user.getRoles().stream().map(Role::getName).collect(toList());
    }


    public void claimTask(User user, String taskId) {
        Task task = getTask(taskId);
        taskChecker.checkUserAuthorized(user, task);
        taskService.claim(task.getId(), user.getUsername());
        log.info("Task '{}' claimed", taskId);
    }

    public void completeTask(User user, String taskId) {
        Task task = getTask(taskId);
        if (!task.getAssignee().equalsIgnoreCase(user.getUsername())) {
            throw new ForbiddenException("User not authorized to complete task");
        }
        taskService.complete(taskId);
        log.info("task completed");
    }

    private Task getTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResourceNotFound("Task with id '{}' does not exist");
        }
        return task;
    }


    public void unclaim(User user, String taskId) {
        Task task = getTask(taskId);
        taskChecker.checkUserAuthorized(user, task);
        taskService.setAssignee(task.getId(), null);
        log.info("Task '{}' unclaimed");
    }

    public Page<Task> query(User user, TaskQueryDto queryDto, Pageable pageable) {
        TaskQuery taskQuery = queryDto.toQuery(processEngine);

        //filter results based on roles or tasks assigned to current user
        taskQuery = taskQuery.taskCandidateGroupIn(resolveCandidateGroups(user))
                .includeAssignedTasks()
                .or()
                .taskAssignee(user.getUsername())
                .endOr();

        long totalResults = taskQuery.count();
        List<Task> tasks = taskQuery.listPage(pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalResults);
    }

    public Task task(User user, String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new ResourceNotFound("Task not found");
        }
        taskChecker.checkUserAuthorized(user, task);
        return task;
    }
}
