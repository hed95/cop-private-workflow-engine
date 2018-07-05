package uk.gov.homeoffice.borders.workflow.task.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.util.UriUtils;
import uk.gov.homeoffice.borders.workflow.ExceptionHandler;
import uk.gov.homeoffice.borders.workflow.InternalWorkflowException;
import uk.gov.homeoffice.borders.workflow.task.NotifyFailureException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CamundaSelector(event = TaskListener.EVENTNAME_CREATE,
        type = ActivityTypes.TASK_USER_TASK)
@Slf4j
@AllArgsConstructor
public class NotificationTaskEventListener extends ReactorTaskListener {

    private static final String SUBJECT = "subject";
    private NotificationClient notificationClient;
    private String emailNotificationTemplateId;
    private String smsNotificationTemplateId;
    private ExceptionHandler exceptionHandler;
    private ObjectMapper objectMapper;
    private static final String NOTIFICATION_VARIABLE_NAME = "notification";


    @Override
    @Retryable(value = {NotifyFailureException.class}, backoff = @Backoff(
            delay = 1000L
    ))
    public void notify(DelegateTask delegateTask) {
        if (isNotificationTaskType(delegateTask)) {
            Notification notification = getNotification(delegateTask);
            Priority priority = notification.getPriority();
            boolean notificationBoost = priority.isNotificationBoost();
            Map<String, String> variables = new HashMap<>();
            variables.put(SUBJECT, notification.getSubject());
            variables.put("payload", notification.getPayload().toString());
            String externalLink = StringUtils.isNotEmpty(notification.getExternalLink()) ?
                    resolveExternalLink(notification, delegateTask) : "";
            variables.put("externalLink", externalLink);

            try {
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
                        variables.put(SUBJECT, "URGENT: " + notification.getSubject());
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
                        variables.put(SUBJECT, "EMERGENCY: " + notification.getSubject());
                        sendEmail(notification, variables, reference);
                        sendSMS(notification, variables, reference);
                }
                saveNotification(delegateTask, notification);
            } catch (Exception e) {
                log.error("Unable to create notification ", e);
            }
        }
    }

    private String resolveExternalLink(Notification notification, DelegateTask delegateTask) {
        String result = notification.getExternalLink().replace("%taskId%", delegateTask.getId());
        result = result.replace("%processInstanceId%", delegateTask.getProcessInstanceId());
        return result;
    }


    private void sendSMS(Notification notification, Map<String, String> variables, String reference) {
        try {
            SendSmsResponse sendSmsResponse = notificationClient.sendSms(smsNotificationTemplateId, notification.getMobile(), variables, reference);
            notification.setSmsNotificationId(sendSmsResponse.getNotificationId().toString());
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
            SendEmailResponse sendEmailResponse = notificationClient.sendEmail(emailNotificationTemplateId,
                    notification.getEmail(), variables, reference);
            notification.setEmailNotificationId(sendEmailResponse.getNotificationId().toString());
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
                getVariableTyped(NOTIFICATION_VARIABLE_NAME, true);
        if (notification.getValue() instanceof SpinJsonNode) {
            SpinJsonNode node = (SpinJsonNode) notification.getValue();
            try {
                return objectMapper.readValue(node.toString(), Notification.class);
            } catch (IOException e) {
                throw new InternalWorkflowException(e);
            }
        } else {
            return (Notification) notification.getValue();
        }
    }

    private void saveNotification(DelegateTask delegateTask, Notification notification) {
        TypedValue type = delegateTask.
                getVariableTyped(NOTIFICATION_VARIABLE_NAME, true);
        if (type.getValue() instanceof SpinJsonNode) {
            delegateTask.setVariable(NOTIFICATION_VARIABLE_NAME, Spin.JSON(notification));
        } else {
            ObjectValue notificationObjectValue =
                    Variables.objectValue(notification)
                            .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                            .create();
            delegateTask.setVariable(NOTIFICATION_VARIABLE_NAME, notificationObjectValue);
        }
    }


    private boolean isNotificationTaskType(DelegateTask delegateTask) {
        String taskType = (String) delegateTask.getVariable("taskType");
        return taskType != null && taskType.equalsIgnoreCase(NOTIFICATION_VARIABLE_NAME);
    }

}
