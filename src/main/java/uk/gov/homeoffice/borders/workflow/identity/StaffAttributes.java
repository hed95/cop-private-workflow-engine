package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
class StaffAttributes {

    private String id;
    private String grade;
    private Integer departmentCode;
    @JsonProperty("securitycleared")
    private Boolean securityCleared;
    private String email;
    @JsonProperty("securitycleareddate")
    private Date securityClearedDate;
}
