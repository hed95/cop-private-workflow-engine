package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private Keycloak keycloak;
    private String realm;

    public User findByUserId(String userId) {
        UserRepresentation keycloakUser = keycloak.realm(realm).users().get(userId).toRepresentation();

        List<String> groupsForUser = keycloakUser.getGroups();
        List<Group> groups = toGroups(groupsForUser);
        return toUser(keycloakUser, groups);

    }

    private List<Group> toGroups(List<String> groupsForUser) {
        return getGroups().stream().filter(
                g -> groupsForUser.contains(g.getId())
        ).collect(toList());
    }

    private User toUser(UserRepresentation keycloakUser, List<Group> groups) {
        User user = new User();
        user.setGroups(groups);
        user.setId(keycloakUser.getId());
        user.setUsername(keycloakUser.getUsername());
        user.setEmail(keycloakUser.getEmail());
        user.setFirstName(keycloakUser.getFirstName());
        user.setLastName(keycloakUser.getLastName());
        return user;
    }

    private List<Group> getGroups() {
        return keycloak.realm(realm).groups().groups().
                stream()
                .map(Group::toGroup).collect(toList());
    }

    public List<User> allUsers() {
        return keycloak
                .realm(realm)
                .users()
                .list()
                .stream()
                .map(u -> toUser(u, toGroups(u.getGroups()))).collect(toList());


    }
}
