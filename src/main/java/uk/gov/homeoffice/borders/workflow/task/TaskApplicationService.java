package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.homeoffice.borders.workflow.identity.Role;
import uk.gov.homeoffice.borders.workflow.identity.User;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private TaskService taskService;
    private TaskSortExecutor taskSortExecutor;

    public Page<Task> tasks(@NotNull User user, Pageable pageable) {
        String taskAssignee = user.getUsername();

        TaskQuery taskQuery = taskService.createTaskQuery()
                .initializeFormKeys()
                .or();

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            taskQuery.taskCandidateGroupIn(resolveCandidateGroups(user))
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
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
    }


}
