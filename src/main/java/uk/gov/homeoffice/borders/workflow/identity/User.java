package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User implements org.camunda.bpm.engine.identity.User {

    @JsonProperty("personid")
    private String id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;
    @JsonProperty("staffattributes")
    private StaffAttributes staffAttributes;
    private Team team;

    public String getEmail() {
        return staffAttributes.getEmail();
    }

    public void setEmail(String email) {
        if (staffAttributes != null) {
            staffAttributes.setEmail(email);
        } else {
            staffAttributes = new StaffAttributes();
            staffAttributes.setEmail(email);
        }
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }


    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    public boolean isMemberOf(String teamId) {
        long count = Team.flatten(this.getTeam())
                .filter(t -> t.getId().equalsIgnoreCase(teamId)).count();
        return count == 1;
    }

}
