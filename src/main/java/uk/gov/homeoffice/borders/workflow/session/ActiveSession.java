package uk.gov.homeoffice.borders.workflow.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
@Data
public class ActiveSession {

    @JsonProperty("sessionid")
    @NotNull
    private String sessionId;
    @JsonProperty("personid")
    private String personId;
    private String email;
    @JsonProperty("teamid")
    @NotNull
    private String teamId;
    @JsonProperty("endtime")
    @NotNull
    private Date endTime;
    private String phone;
    @JsonProperty("locationid")
    private String locationId;
    @JsonProperty("regionid")
    private String regionId;
    @JsonProperty("setregionasdefault")
    private String setRegionAsDefault;
    @JsonProperty("setlocationasdefault")
    private String setLocationAsDefault;
    @JsonProperty("setteamasdefault")
    private String setTeamAsDefault;
    @JsonProperty("sessiontype")
    private String sessionType = "workflow";
    @JsonProperty("shiftinhours")
    private Integer shiftInHours;
    @JsonProperty("shiftinminutes")
    private Integer shiftInMinutes;
    private String firstName;
    private String lastName;
    private String grade;
}
