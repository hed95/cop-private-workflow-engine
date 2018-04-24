package uk.gov.homeoffice.borders.workflow.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
@Data
@JsonIgnoreProperties
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
    @JsonProperty("starttime")
    @NotNull
    private Date startTime;
    @JsonProperty("endtime")
    private Date endTime;
    private String phone;
    @JsonProperty("sessiontype")
    private String sessionType = "workflow";
    @JsonProperty("shifthours")
    private Integer shiftHours;
    @JsonProperty("shiftminutes")
    private Integer shiftMinutes;


    //optional
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
    private String firstName;
    private String lastName;
    private String grade;
    @JsonProperty("securitycleareddate")
    private Date securityClearedDate;
    @JsonProperty("securitycleared")
    private boolean securityCleared;

}
