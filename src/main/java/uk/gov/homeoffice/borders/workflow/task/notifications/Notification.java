package uk.gov.homeoffice.borders.workflow.task.notifications;

import lombok.Data;

@Data
public class Notification {
    private String name;
    private Object payload;
    private String region;
    private String location;
    private String team;
    private String assignee;
    private Priority priority;
    private boolean acknowledgementRequired;


}
