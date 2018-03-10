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

    private TaskService taskService;
    private RuntimeService runtimeService;
    private UserService userService;

    public Page<Task> notifications(User user, Pageable pageable) {
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey("notifications")
                .processVariableValueEquals("type", "notifications")
                .taskAssignee(user.getUsername())
                .orderByTaskCreateTime()
                .desc();
        Long totalCount = query.count();
        List<Task> tasks = query
                .listPage(pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(tasks, pageable, totalCount);
    }

    public ProcessInstance create(Notification notification) {

        List<User> candidateUsers = userService.allUsers(); //filter

        List<Notification> notifications = candidateUsers.stream().map(u -> {
            Notification updated = new Notification();
            updated.setPayload(notification.getPayload());
            updated.setName(notification.getName());
            updated.setAssignee(u.getUsername());
            updated.setPriority(notification.getPriority());
            return updated;

        }).collect(toList());

        ObjectValue notificationObjectValue =
                Variables.objectValue(notifications)
                        .serializationDataFormat("application/json")
                        .create();

        Map<String,Object> variables = new HashMap<>();
        variables.put("notifications", notificationObjectValue);
        variables.put("type", "notifications");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("notifications",
                variables);



        return processInstance;

    }
}
