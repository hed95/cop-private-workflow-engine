package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskApplicationService {

    private TaskService taskService;

    public List<Task> tasks(User user) {

        String taskAssignee = user.getUsername();

        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(taskAssignee)
                .or()
                .taskCandidateGroupIn(resolveCandidateGroups(user))
                .or()
                .taskCandidateUser(resolveCandidateUser(user))
                .endOr()
                .list();

        return Collections.unmodifiableList(tasks);
    }


    private List<String> resolveCandidateGroups(User user) {
        return null;
    }

    private String resolveCandidateUser(User user) {
        return user.getUsername();
    }

}
