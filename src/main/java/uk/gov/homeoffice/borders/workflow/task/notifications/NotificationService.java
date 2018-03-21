package uk.gov.homeoffice.borders.workflow.task.notifications;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.identity.UserService;
import uk.gov.homeoffice.borders.workflow.task.TaskApplicationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    private static final String NOTIFICATIONS = "notifications";

    private TaskService taskService;
    private RuntimeService runtimeService;
    private UserService userService;
    private TaskApplicationService taskApplicationService;

    public Page<Task> notifications(User user, Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(NOTIFICATIONS)
                .processVariableValueEquals("type", NOTIFICATIONS)
                .taskAssignee(user.getEmail())
                .orderByTaskCreateTime()
                .desc();
        Long totalCount = query.count();

        int pageNumber = taskApplicationService.calculatePageNumber(pageable);

        List<Task> tasks = query
                .listPage(pageNumber, pageable.getPageSize());


        return new PageImpl<>(tasks, new PageRequest(pageable.getPageNumber(), pageable.getPageSize()), totalCount);
    }


    public ProcessInstance create(Notification notification) {

        List<User> candidateUsers = userService.allUsers().stream()
                .filter(u -> filterByRegion(u, notification.getRegion())
                        || filterByLocation(u, notification.getLocation())
                        || filterByTeam(u, notification.getTeam())).collect(toList());

        List<Notification> notifications = candidateUsers.stream().map(u -> {
            Notification updated = new Notification();
            updated.setPayload(notification.getPayload());
            updated.setSubject(notification.getSubject());
            updated.setPriority(notification.getPriority());
            updated.setEmail(u.getEmail());
            updated.setMobile(u.getPhone());
            return updated;

        }).collect(toList());

        ObjectValue notificationObjectValue =
                Variables.objectValue(notifications)
                        .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                        .create();

        Map<String, Object> variables = new HashMap<>();
        variables.put("notifications", notificationObjectValue);
        variables.put("type", NOTIFICATIONS);

        return runtimeService.startProcessInstanceByKey(NOTIFICATIONS,
                variables);

    }

    private boolean filterByTeam(User user, String team) {
        return team != null && Team.flatten(user.getTeam()).filter(t -> team.equalsIgnoreCase(t.getName())).count() == 1;

    }

    private boolean filterByLocation(User user, String location) {
        return location != null && Team.flatten(user.getTeam()).filter(t -> location.equalsIgnoreCase(t.getLocation())).count() == 1;

    }

    private boolean filterByRegion(User user, String region) {
        return region != null && Team.flatten(user.getTeam()).filter(t -> region.equalsIgnoreCase(t.getRegion())).count() == 1;

    }

    public void cancel(String processInstanceId, String reason) {
        if (runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count() != 0) {
            runtimeService.deleteProcessInstance(processInstanceId, reason);
        }
    }

    public String acknowledge(User user, String taskId) {
        Task task = taskApplicationService.getTask(user, taskId);
        taskService.claim(task.getId(), user.getEmail());
        taskService.complete(task.getId());
        log.info("Notification acknowledged.");
        return task.getId();
    }
}
