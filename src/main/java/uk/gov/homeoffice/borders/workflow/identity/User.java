package uk.gov.homeoffice.borders.workflow.identity;

import org.springframework.util.CollectionUtils;
import uk.gov.homeoffice.borders.workflow.identity.Group;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class User implements org.camunda.bpm.engine.identity.User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<Group> groups;
    private String username;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    public boolean hasGroup(String groupId) {
        return this.groups != null
                && !CollectionUtils.isEmpty(this.getGroups().stream().filter(g -> g.getId().equals(groupId)).collect(toList()));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
