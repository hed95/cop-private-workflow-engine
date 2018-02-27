package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class GroupService {

    private Keycloak keycloak;
    private String realm;

    public Group findById(String groupId) {
        GroupRepresentation groupRepresentation = keycloak.realm(realm).groups().group(groupId).toRepresentation();
        return Group.toGroup(groupRepresentation);
    }


    public List<Group> allGroups() {
        return keycloak.realm(realm).groups()
                .groups().stream()
                .map(Group::toGroup).collect(toList());
    }
}
