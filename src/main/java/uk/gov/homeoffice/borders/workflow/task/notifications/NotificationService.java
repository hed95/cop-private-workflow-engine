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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.identity.TeamService;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.identity.UserService;

import java.util.Collections;
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

    public Page<Task> notifications(User user, Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(NOTIFICATIONS)
                .processVariableValueEquals("type", NOTIFICATIONS)
                .taskAssignee(user.getUsername())
                .orderByTaskCreateTime()
                .desc();
        Long totalCount = query.count();
        List<Task> tasks = query
                .listPage(pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalCount);
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
            updated.setAssignee(u.getUsername());
            updated.setPriority(notification.getPriority());
            updated.setEmail(u.getEmail());
            updated.setMobile(u.getMobile());
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
        return team != null && user.getTeams().stream().filter(t -> team.equalsIgnoreCase(t.getName())).count() == 1;

    }

    private boolean filterByLocation(User user, String location) {
        return location != null && user.getTeams().stream().filter(t -> location.equalsIgnoreCase(t.getLocation())).count() == 1;

    }

    private boolean filterByRegion(User user, String region) {
        return region != null && user.getTeams().stream().filter(t -> region.equalsIgnoreCase(t.getRegion())).count() == 1;

    }

    public void cancel(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }
}
