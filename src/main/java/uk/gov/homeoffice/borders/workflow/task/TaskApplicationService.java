package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.homeoffice.borders.workflow.identity.Group;
import uk.gov.homeoffice.borders.workflow.identity.User;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private TaskService taskService;

    public List<Task> tasks(@NotNull User user) {
        String taskAssignee = user.getUsername();

        TaskQuery taskQuery = taskService.createTaskQuery()
                .initializeFormKeys()
                .or();

        if (!CollectionUtils.isEmpty(user.getGroups())) {
            taskQuery.taskCandidateGroupIn(resolveCandidateGroups(user))
                    .includeAssignedTasks();
        }
        List<Task> tasks = taskQuery.taskAssignee(taskAssignee).endOr().list();

        return Collections.unmodifiableList(tasks);
    }


    private List<String> resolveCandidateGroups(User user) {
        return user.getGroups().stream().map(Group::getName).collect(Collectors.toList());
    }


}
