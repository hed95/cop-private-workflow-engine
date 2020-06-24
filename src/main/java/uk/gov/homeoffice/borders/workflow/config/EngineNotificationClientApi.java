package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.json.JSONObject;
import uk.gov.service.notify.*;

import java.util.Map;

/**
 * Wrapped client that can be used inside an executing BPMN
 * If a call fails then an incident will be raised in Cockpit
 * against the execution id. The reference that needs to be passed
 * in should be the current execution id.
 */
@Slf4j
public class EngineNotificationClientApi implements NotificationClientApi {

    private final NotificationClientApi notificationClientApi;
    private final RuntimeService runtimeService;

    public static final String NOTIFICATION_FAILURE = "NOTIFICATION_FAILURE";

    public EngineNotificationClientApi(NotificationClientApi notificationClientApi,
                                       RuntimeService runtimeService) {
        this.notificationClientApi = notificationClientApi;
        this.runtimeService = runtimeService;
    }

    @Override
    public SendEmailResponse sendEmail(String templateId,
                                       String emailAddress,
                                       Map<String, String> personalisation,
                                       String executionId) throws NotificationClientException {
        try {
            return notificationClientApi.sendEmail(
                    templateId,
                    emailAddress,
                    personalisation,
                    executionId
            );
        } catch (Exception e) {
            handleException(Map.of(
                    "exception", e.getMessage(),
                    "templateId", templateId,
                    "emailAddress", emailAddress,
                    "executionId", executionId
            ), e);
            throw e;
        }

    }

    @Override
    public SendEmailResponse sendEmail(String templateId,
                                       String emailAddress,
                                       Map<String, String> personalisation,
                                       String executionId,
                                       String emailReplyToId) throws NotificationClientException {

        try {
            return notificationClientApi.sendEmail(templateId,
                    emailAddress,
                    personalisation,
                    executionId,
                    emailReplyToId);
        } catch (Exception e) {
            handleException(Map.of(
                    "exception", e.getMessage(),
                    "templateId", templateId,
                    "emailAddress", emailAddress,
                    "executionId", executionId,
                    "emailRelyToId", emailReplyToId
            ), e);
            throw e;
        }
    }

    @Override
    public SendSmsResponse sendSms(String templateId,
                                   String phoneNumber,
                                   Map<String, String> personalisation,
                                   String executionId) throws NotificationClientException {
        try {
            return notificationClientApi.sendSms(
                    templateId,
                    phoneNumber,
                    personalisation,
                    executionId
            );
        } catch (Exception e) {
            handleException(Map.of(
                    "exception", e.getMessage(),
                    "templateId", templateId,
                    "phoneNumber", phoneNumber,
                    "executionId", executionId
            ), e);
            throw e;
        }
    }

    @Override
    public SendSmsResponse sendSms(String templateId,
                                   String phoneNumber,
                                   Map<String, String> personalisation,
                                   String executionId,
                                   String smsSenderId) throws NotificationClientException {
        try {
            return notificationClientApi.sendSms(
                    templateId,
                    phoneNumber,
                    personalisation,
                    executionId,
                    smsSenderId
            );
        } catch (Exception e) {
            handleException(Map.of(
                    "exception", e.getMessage(),
                    "templateId", templateId,
                    "phoneNumber", phoneNumber,
                    "smsSenderId", smsSenderId,
                    "executionId", executionId
            ), e);
            throw e;
        }
    }

    @Override
    public SendLetterResponse sendLetter(String templateId,
                                         Map<String, String> personalisation,
                                         String executionId) throws NotificationClientException {
       throw new UnsupportedOperationException("Sending a letter is not supported in this version of the client");
    }

    @Override
    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        throw new UnsupportedOperationException("getNotificationById is not supported in this version of the client");
    }

    @Override
    public NotificationList getNotifications(String status,
                                             String notification_type,
                                             String executionId,
                                             String olderThanId) throws NotificationClientException {
        throw new UnsupportedOperationException("getNotifications is not supported in this version of the client");

    }

    @Override
    public Template getTemplateById(String templateId) throws NotificationClientException {
        throw new UnsupportedOperationException("getTemplateById is not supported in this version of the client");

    }

    @Override
    public Template getTemplateVersion(String templateId, int version) throws NotificationClientException {
        throw new UnsupportedOperationException("getTemplateVersion is not supported in this version of the client");

    }

    @Override
    public TemplateList getAllTemplates(String templateType) throws NotificationClientException {
        throw new UnsupportedOperationException("getAllTemplates is not supported in this version of the client");

    }

    @Override
    public TemplatePreview generateTemplatePreview(String templateId,
                                                   Map<String, String> personalisation) throws NotificationClientException {
        throw new UnsupportedOperationException("generateTemplatePreview is not supported in this version of the client");

    }

    @Override
    public ReceivedTextMessageList getReceivedTextMessages(String olderThanId) throws NotificationClientException {
        throw new UnsupportedOperationException("getReceivedTextMessages is not supported in this version of the client");

    }


    private void handleException(Map<String, Object> personalisation,
                                 Exception e) {
        try {
            log.error("Failed to send email '{}'", e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", new JSONObject(personalisation));
            runtimeService.createIncident(
                    NOTIFICATION_FAILURE,
                    personalisation.get("executionId").toString(),
                    e.getMessage(),
                    jsonObject.toString()
            );
        } catch (Exception rex) {
            log.error("Failed to create incident '{}'", rex.getMessage());
        }
    }
}
