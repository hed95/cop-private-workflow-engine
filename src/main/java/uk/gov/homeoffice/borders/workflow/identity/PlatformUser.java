package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformUser implements org.camunda.bpm.engine.identity.User {


    @JsonProperty("staffid")
    private String id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("surname")
    private String lastName;
    private String grade;
    private List<Team> teams;
    private String email;
    @JsonProperty("qualificationtypes")
    private List<Qualification> qualifications = new ArrayList<>();
    private List<String> roles = new ArrayList<>();

    private String adelphi;
    @JsonProperty("linemanagerid")
    private String lineManagerId;

    @JsonProperty("ministryid")
    private String ministryId;
    @JsonProperty("departmentid")
    private String departmentId;
    @JsonProperty("branchid")
    private String branchId;
    @JsonProperty("divisionid")
    private String divisionId;
    @JsonProperty("commandid")
    private String commandId;
    @JsonProperty("gradeid")
    private String gradeId;
    @JsonProperty("identityid")
    private String identityId;
    private ShiftDetails shiftDetails;


    public boolean isServiceUser() {
        Optional<Team> serviceRole = this.getTeams()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase(WorkflowAuthentication.SERVICE_ROLE))
                .findFirst();
        return serviceRole.isPresent();
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Data
    public static class Qualification implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("qualificationtype")
        private String id;
        @JsonProperty("qualificationname")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShiftDetails {

        @JsonProperty("staffid")
        private String staffId;
        @NotNull
        private String email;
        @NotNull
        private String phone;
        @NotNull
        @JsonProperty("locationid")
        private String locationId;
        @JsonProperty("teamid")
        @NotNull
        private String teamId;
        @JsonProperty("team")
        private Object team;
        @JsonProperty("shiftid")
        private String shiftId;
        @NotNull
        @JsonProperty("startdatetime")
        private Date startDateTime;
        @JsonProperty("enddatetime")
        private Date endDateTime;
        @JsonProperty("shifthours")
        private Integer shiftHours;
        @JsonProperty("shiftminutes")
        private Integer shiftMinutes;
        @JsonProperty("gradeid")
        private String gradeId;


        private String currentLocationName;
        private List<String> roles = new ArrayList<>();
    }
}
