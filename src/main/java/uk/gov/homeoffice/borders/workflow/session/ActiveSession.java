package uk.gov.homeoffice.borders.workflow.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
@Data
public class ActiveSession {

    @JsonProperty("sessionid")
    private String sessionId;
    @JsonProperty("personid")
    private String personId;
    private String email;
    @JsonProperty("teamid")
    private String teamId;
    @JsonProperty("endtime")
    private Date endTime;
    private String phone;
    @JsonProperty("locationid")
    private String locationId;
    @JsonProperty("setregionasdefault")
    private String setRegionAsDefault;
    @JsonProperty("setlocationasdefault")
    private String setLocationAsDefault;
    @JsonProperty("setteamasdefault")
    private String setTeamAsDefault;
    @JsonProperty("sessiontype")
    private String sessionType = "workflow";

}
