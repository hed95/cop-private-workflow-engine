package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShiftUser implements org.camunda.bpm.engine.identity.User {

    @JsonProperty("staffid")
    private String id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;
    private String grade;
    private String phone;
    private List<Team> teams;
    private String email;
    @JsonProperty("qualificationtypes")
    private List<Qualification> qualifications = new ArrayList<>();
    private List<String> roles = new ArrayList<>();

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Data
    public static class Qualification implements Serializable{
        private static final long serialVersionUID = 1L;

        @JsonProperty("qualificationtype")
        private String id;
        @JsonProperty("qualificationname")
        private String name;
    }
}
