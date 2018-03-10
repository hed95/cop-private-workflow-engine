package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private Keycloak keycloak;
    private String realm;
    private TeamService teamService;


    public User findByUserId(String userId) {
        UserRepresentation keycloakUser = keycloak.realm(realm).users().get(userId).toRepresentation();

        List<String> groupsForUser = keycloakUser.getGroups() == null ? new ArrayList<>() : keycloakUser.getGroups();
        List<Role> groups = toGroups(groupsForUser);
        return toUser(keycloakUser, groups);

    }

    private List<Role> toGroups(List<String> groupsForUser) {
        return getGroups().stream().filter(
                g -> groupsForUser.contains(g.getId())
        ).collect(toList());
    }

    private User toUser(UserRepresentation keycloakUser, List<Role> groups) {
        User user = new User();
        user.setRoles(groups);
        user.setId(keycloakUser.getId());
        user.setUsername(keycloakUser.getUsername());
        user.setEmail(keycloakUser.getEmail());
        user.setFirstName(keycloakUser.getFirstName());
        user.setLastName(keycloakUser.getLastName());
        List<Team> teams = teamService.findByUser(user);
        user.setTeams(teams);
        return user;
    }

    private List<Role> getGroups() {
        return keycloak.realm(realm).roles().list().
                stream()
                .map(Role::toRole).collect(toList());
    }

    public List<User> allUsers() {
        return keycloak
                .realm(realm)
                .users()
                .list()
                .stream()
                .map(u -> toUser(u, toGroups(u.getGroups() == null ? new ArrayList<>() : u.getGroups()))).collect(toList());


    }

}
