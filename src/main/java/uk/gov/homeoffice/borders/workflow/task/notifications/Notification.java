package uk.gov.homeoffice.borders.workflow.task.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {
    private String subject;
    private Object payload;
    private String locationId;
    private String teamId;
    private Priority priority;
    private String assignee;
    private String email;
    private String mobile;
    private String incidentid;
    private String externalLink;
    private String smsNotificationId;
    private String emailNotificationId;
    private String incidentResponsesId;

}
