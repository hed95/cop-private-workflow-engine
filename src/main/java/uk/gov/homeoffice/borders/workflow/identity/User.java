package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class User implements org.camunda.bpm.engine.identity.User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String grade;
    private String mobile;
    private String username;
    private List<Role> roles = new ArrayList<>();

    private List<Team> teams = new ArrayList<>();
    private Shift shift;


    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    public boolean hasRole(String groupId) {
        return this.roles != null
                && !CollectionUtils.isEmpty(this.getRoles().stream().filter(g -> g.getId().equals(groupId)).collect(toList()));
    }

}
