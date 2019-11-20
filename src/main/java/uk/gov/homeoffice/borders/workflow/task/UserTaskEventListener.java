package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.TeamsDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@CamundaSelector(type = ActivityTypes.TASK_USER_TASK)
@Slf4j
@AllArgsConstructor
public class UserTaskEventListener extends ReactorTaskListener {

    private SimpMessagingTemplate messagingTemplate;
    private RefDataUrlBuilder refDataUrlBuilder;
    private RestTemplate restTemplate;

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            final String assignee = delegateTask.getAssignee();
            final List<String> teamCodes = delegateTask.
                    getCandidates()
                    .stream()
                    .map(IdentityLink::getGroupId)
                    .collect(toList());


            final List<String> candidateUsers = delegateTask.
                    getCandidates()
                    .stream()
                    .map(IdentityLink::getUserId)
                    .filter(id -> id != null && !id.equalsIgnoreCase(""))
                    .collect(toList());


            final List<String> teamIds = new ArrayList<>();
            if (!teamCodes.isEmpty()) {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(httpHeaders);

                TeamsDto response = restTemplate
                        .getForEntity(refDataUrlBuilder
                                .teamByCodeIds(teamCodes.toArray(new String[]{})), TeamsDto.class)
                        .getBody();

                if (response != null) {
                    teamIds.addAll(response.getData().stream()
                            .filter(team -> !team.getCode().equalsIgnoreCase("STAFF"))
                            .map(Team::getId).collect(Collectors.toList()));
                    log.info("teamids {}", teamIds);
                }
            }

            final String taskId = delegateTask.getId();
            TaskReference taskReference = new TaskReference();
            taskReference.setId(taskId);
            taskReference.setStatus(delegateTask.getEventName());

            registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCompletion(int status) {
                    super.afterCompletion(status);
                    candidateUsers.forEach(user -> {
                        if (user != null && !user.equalsIgnoreCase("")) {
                            messagingTemplate.convertAndSendToUser(user,
                                    "/queue/task", taskReference);
                        }
                    });

                    ofNullable(assignee)
                                        .ifPresent(a -> messagingTemplate.convertAndSendToUser(a,
                                    "/queue/task", taskReference));

                    teamIds.forEach(team ->
                            messagingTemplate.convertAndSend(format("/topic/task/%s", team), taskReference));
                }

            });
        } catch (
                Exception e) {
            log.error("Exception occurred while trying to emit websocket task creation", e);
        }
    }
}
