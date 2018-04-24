package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class StaffAttributes {

    @JsonProperty("staffattributesid")
    private String id;
    private String grade;
    @JsonProperty("securitycleared")
    private Boolean securityCleared;
    private String email;
    private String phone;
    @JsonProperty("securitycleareddate")
    private Date securityClearedDate;
    @JsonProperty("personid")
    private String userId;
    @JsonProperty("defaultteamid")
    private String defaultTeamId;
    @JsonProperty("defaultlocationid")
    private String defaultLocationId;
    @JsonProperty("defaultregionid")
    private String defaultRegionId;
}
