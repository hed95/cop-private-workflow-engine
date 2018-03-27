package uk.gov.homeoffice.borders.workflow.session;

import lombok.Data;

import java.util.Date;
@Data
public class ActiveSession {

    private String activeSessionId;
    private String personId;
    private String email;
    private String teamId;
    private String keycloakSessionId;
    private Date endTime;

}
