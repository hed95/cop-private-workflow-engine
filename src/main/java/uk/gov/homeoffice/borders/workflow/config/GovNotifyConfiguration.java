package uk.gov.homeoffice.borders.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.homeoffice.borders.workflow.exception.ExceptionHandler;
import uk.gov.homeoffice.borders.workflow.task.notifications.NotificationTaskEventListener;
import uk.gov.service.notify.*;

import java.util.Map;

@Configuration
public class GovNotifyConfiguration {

    @Value("${gov.notify.api.notification.emailTemplateId}")
    private String emailNotificationTemplateId;

    @Value("${gov.notify.api.notification.smsTemplateId}")
    private String smsNotificationTemplateId;

    @Value("${public-ui.protocol}")
    private String publicUIProtocol;

    @Value("${public-ui.text-protocol}")
    private String publicUITextProtocol;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonJsonDataFormat formatter;


    @Bean
    public ExceptionHandler exceptionHandler() {
        return new ExceptionHandler(objectMapper);
    }

    @Autowired
    private NotificationClientApi notificationClient;

    @Bean
    public NotificationTaskEventListener notificationTaskEventListener() {
        return new NotificationTaskEventListener(notificationClient, emailNotificationTemplateId,
                smsNotificationTemplateId, exceptionHandler(), formatter, publicUIProtocol, publicUITextProtocol);
    }


    @Configuration
    @Profile({"test"})
    public static class LocalGovNotifyConfiguration {

        public static class StubGovNotifyClient implements NotificationClientApi {

            @Override
            public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, String> personalisation, String reference) throws NotificationClientException {
                return null;
            }

            @Override
            public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, String> personalisation, String reference, String emailReplyToId) throws NotificationClientException {
                return null;
            }

            @Override
            public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, String> personalisation, String reference) throws NotificationClientException {
                return null;
            }

            @Override
            public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, String> personalisation, String reference, String smsSenderId) throws NotificationClientException {
                return null;
            }

            @Override
            public SendLetterResponse sendLetter(String templateId, Map<String, String> personalisation, String reference) throws NotificationClientException {
                return null;
            }

            @Override
            public Notification getNotificationById(String notificationId) throws NotificationClientException {
                return null;
            }

            @Override
            public NotificationList getNotifications(String status, String notification_type, String reference, String olderThanId) throws NotificationClientException {
                return null;
            }

            @Override
            public Template getTemplateById(String templateId) throws NotificationClientException {
                return null;
            }

            @Override
            public Template getTemplateVersion(String templateId, int version) throws NotificationClientException {
                return null;
            }

            @Override
            public TemplateList getAllTemplates(String templateType) throws NotificationClientException {
                return null;
            }

            @Override
            public TemplatePreview generateTemplatePreview(String templateId, Map<String, String> personalisation) throws NotificationClientException {
                return null;
            }

            @Override
            public ReceivedTextMessageList getReceivedTextMessages(String olderThanId) throws NotificationClientException {
                return null;
            }
        }

        @Bean
        public NotificationClientApi notificationClient() {
            return new StubGovNotifyClient();
        }

    }

    @Configuration
    @Profile({ "!test"})
    public static class NonLocalGovNotifyConfiguration {
        @Value("${gov.notify.api.key}")
        private String notificationApiKey;

        @Bean
        public NotificationClientApi notificationClient() {
            return new NotificationClient(notificationApiKey);
        }

    }
}
