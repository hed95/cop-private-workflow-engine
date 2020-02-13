package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements org.camunda.bpm.engine.identity.Group {
    @JsonProperty(required = true)
    private String id;
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(required = true)
    private String code;
    private String type;
    private String description;
    @JsonProperty("costcentrecode")
    private String costCentreCode;
    @JsonProperty("parentteamid")
    private String parentTeamId;
    @JsonProperty("bffunctiontypeid")
    private String bfFunctionTypeId;
    @JsonProperty("ministryid")
    private String ministryId;
    @JsonProperty("departmentid")
    private String departmentId;
    @JsonProperty("directorateid")
    private String directorateId;
    @JsonProperty("branchid")
    private String branchId;
    @JsonProperty("divisionid")
    private String divisionId;
    @JsonProperty("commandid")
    private String commandId;
    @JsonProperty("validfrom")
    private Date validFrom;
    @JsonProperty("validto")
    private Date validTo;
}
