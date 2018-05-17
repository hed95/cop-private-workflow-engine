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
    @NotNull @JsonProperty("enddatetime")
    private Date endDateTime;
    @NotNull @JsonProperty("startdatetime")
    private Date startDateTime;
    @JsonProperty("shifthours")
    private Integer shiftHours;
    @JsonProperty("shiftminutes")
    private Integer shiftMinutes;

    @NotNull @JsonProperty("teamid")
    private String teamId;
    @JsonProperty("commandid")
    private String commandId;
    @JsonProperty("subcommandid")
    private String subCommandId;
    @JsonProperty("locationid")
    private String locationId;

    @JsonProperty("staffid")
    private String staffId;
    private String phone;
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


}
