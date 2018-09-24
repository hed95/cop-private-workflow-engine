package uk.gov.homeoffice.borders.workflow.shift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
@Data
@JsonIgnoreProperties
public class ShiftInfo {

    @JsonProperty("shiftid")
    private String shiftId;
    @JsonProperty("enddatetime")
    private Date endDateTime;
    @NotNull @JsonProperty("startdatetime")
    private Date startDateTime;
    @JsonProperty("shifthours")
    private Integer shiftHours;
    @JsonProperty("shiftminutes")
    private Integer shiftMinutes;

    @NotNull @JsonProperty("teamid")
    private String teamId;
    @NotNull
    @JsonProperty("commandid")
    private String commandId;
    @NotNull
    @JsonProperty("subcommandid")
    private String subCommandId;
    @NotNull
    @JsonProperty("locationid")
    private String locationId;
    @NotNull
    @JsonProperty("currentlocationid")
    private String currentLocationId;

    @JsonProperty("staffid")
    private String staffId;
    @NotNull
    private String phone;
    @NotNull
    private String email;
    @JsonProperty("gradetypeid")
    private String gradeTypeId;

    @JsonProperty("setdetfaultteamid")
    private String setDefaultTeamId;
    @JsonProperty("setdefaultlocationid")
    private String setDefaultLocationId;
    @JsonProperty("setdefaultcommandid")
    private String setDefaultCommandId;
    @JsonProperty("setdefaultsubcommandid")
    private String setDefaultSubCommandId;
    @JsonProperty("firstname")
    private String firstName;
    private String surname;
    private String currentLocationName;


}
