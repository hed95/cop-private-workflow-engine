package uk.gov.homeoffice.borders.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.homeoffice.borders.workflow.exception.ExceptionHandler;
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

    @Value("${public-ui.protocol}")
    private String publicUIProtocol;

    @Value("${public-ui.text-protocol")
    private String publicUITextProtocol;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonJsonDataFormat formatter;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(notificationApiKey);
    }

    @Bean
    public ExceptionHandler exceptionHandler() {
        return new ExceptionHandler(objectMapper);
    }

    @Bean
    public NotificationTaskEventListener notificationTaskEventListener(NotificationClient notificationClient) {
        return new NotificationTaskEventListener(notificationClient, emailNotificationTemplateId,
                smsNotificationTemplateId, exceptionHandler(), formatter, publicUIProtocol, publicUITextProtocol);
    }
}
