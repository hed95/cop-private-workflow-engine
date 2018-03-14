package uk.gov.homeoffice.borders.workflow.task.notifications;

import lombok.Data;

@Data
public class Notification {
    private String subject;
    private Object payload;
    private String region;
    private String location;
    private String team;
    private Priority priority;

    private String assignee;
    private String email;
    private String mobile;
}
