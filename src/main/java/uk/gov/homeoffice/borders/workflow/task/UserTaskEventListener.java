package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@CamundaSelector(event = TaskListener.EVENTNAME_CREATE,
        type = ActivityTypes.TASK_USER_TASK)
@Slf4j
@AllArgsConstructor
public class UserTaskEventListener extends ReactorTaskListener {

    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String assignee = delegateTask.getAssignee();

            List<String> teamCodes = delegateTask.
                    getCandidates()
                    .stream()
                    .map(IdentityLink::getGroupId)
                    .collect(toList());

            String taskId = delegateTask.getId();

            ofNullable(assignee)
                    .ifPresent(a -> messagingTemplate.convertAndSendToUser(assignee,
                            "/topic/task", taskId));

            teamCodes.forEach(team ->
                    messagingTemplate.convertAndSend(format("/topic/task/%s", team), taskId));

        } catch (
                Exception e) {
            log.error("Exception occurred while trying to emit websocket task creation", e);
        }
    }
}
