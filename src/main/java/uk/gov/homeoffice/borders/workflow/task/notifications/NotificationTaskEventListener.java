package uk.gov.homeoffice.borders.workflow.task.notifications;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import uk.gov.homeoffice.borders.workflow.ExceptionHandler;
import uk.gov.homeoffice.borders.workflow.task.NotifyFailureException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

@CamundaSelector(event = TaskListener.EVENTNAME_CREATE)
@Slf4j
@AllArgsConstructor
public class NotificationTaskEventListener extends ReactorTaskListener {

    private NotificationClient notificationClient;
    private String emailNotificationTemplateId;
    private String smsNotificationTemplateId;
    private ExceptionHandler exceptionHandler;

    @Override
    @Retryable(value = {NotifyFailureException.class}, backoff = @Backoff(
            delay = 1000L
    ))
    public void notify(DelegateTask delegateTask) {

        if (processIsNotification(delegateTask)) {
            Notification notification = getNotification(delegateTask);
            Priority priority = notification.getPriority();
            boolean notificationBoost = priority.isNotificationBoost();
            Map<String, String> variables = new HashMap<>();
            variables.put("subject", notification.getSubject());
            variables.put("payload", notification.getPayload().toString());

            String reference = String.format("/api/workflow/notifications/%s", delegateTask.getProcessInstanceId());
            switch (priority.getType()) {
                case STANDARD:
                    log.info("Standard Priority defined");
                    if (notificationBoost) {
                        log.info("Standard Priority with Boost enabled (sendEmail)");
                        sendEmail(notification, variables, reference);
                    }
                    break;
                case URGENT:
                    log.info("Urgent Priority defined");
                    variables.put("subject", "URGENT: " + notification.getSubject());
                    if (!notificationBoost) {
                        sendEmail(notification, variables, reference);
                    } else {
                        log.info("Urgent Priority with Boost enabled (sendSMS and sendEmail)");
                        sendEmail(notification, variables, reference);
                        sendSMS(notification, variables, reference);
                    }
                    break;
                default:
                    log.info("Emergency Priority defined..applying default boost (sendSMS and sendEmail)");
                    variables.put("subject", "EMERGENCY: " + notification.getSubject());
                    sendEmail(notification, variables, reference);
                    sendSMS(notification, variables, reference);
            }
        }
    }

    private void sendSMS(Notification notification, Map<String, String> variables, String reference) {
        try {
            notificationClient.sendSms(smsNotificationTemplateId, notification.getMobile(), variables, reference);
            log.info("SMS notification sent");
        } catch (NotificationClientException e) {
            if (HttpStatus.valueOf(e.getHttpResult()).is5xxServerError()) {
                throw new NotifyFailureException(e);
            } else {
                exceptionHandler.registerNotification(e, notification);
            }
        }
    }

    private void sendEmail(Notification notification, Map<String, String> variables, String reference) {
        try {
            notificationClient.sendEmail(emailNotificationTemplateId,
                    notification.getEmail(), variables, reference);
            log.info("Email notification sent");
        } catch (NotificationClientException e) {
            if (HttpStatus.valueOf(e.getHttpResult()).is5xxServerError()) {
                throw new NotifyFailureException(e);
            } else {
                exceptionHandler.registerNotification(e, notification);
            }
        }

    }

    private Notification getNotification(DelegateTask delegateTask) {
        TypedValue notification = delegateTask.
                getVariableTyped("notification", true);
        return (Notification) notification.getValue();
    }

    private boolean processIsNotification(DelegateTask delegateTask) {
        return delegateTask.getProcessDefinitionId().startsWith("notifications:");
    }
}
