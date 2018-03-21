package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
public class User implements org.camunda.bpm.engine.identity.User {

    private String id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;
    private String email;
    @JsonProperty("staffattributes")
    private StaffAttributes staffAttributes;
    private String phone;
    private Team team;

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
                .filter(t -> t.getName().equalsIgnoreCase(teamId)).count();
        return count == 1;
    }


}
