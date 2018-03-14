package uk.gov.homeoffice.borders.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.homeoffice.borders.workflow.ExceptionHandler;
import uk.gov.homeoffice.borders.workflow.task.notifications.NotificationTaskEventListener;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class GovNotifyConfiguration {

    @Value("${gov.notify.api.key}")
    private String notificationApiKey;

    @Value("${gov.notify.api.notification.emailTemplateId}")
    private String emailNotificationTemplateId;

    @Value("${gov.notify.api.notification.smsTemplateId}")
    private String smsNotificationTemplateId;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(notificationApiKey);
    }

    @Bean
    public ExceptionHandler exceptionHandler() {
        return new ExceptionHandler(objectMapper);
    }

    @Bean
    public NotificationTaskEventListener notificationTaskEventListener() {
        return new NotificationTaskEventListener(notificationClient(), emailNotificationTemplateId,
                smsNotificationTemplateId, exceptionHandler());
    }
}
