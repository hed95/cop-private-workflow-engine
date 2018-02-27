package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class Group implements org.camunda.bpm.engine.identity.Group {

    private String id;
    private String name;
    private String type;
    private List<String> roles;
    private List<Group> subGroups;


    public static Group toGroup(GroupRepresentation groupRepresentation) {
        Group group = new Group();

        group.setId(groupRepresentation.getId());
        group.setName(groupRepresentation.getName());
        group.setType(groupRepresentation.getPath());
        group.setRoles(groupRepresentation.getRealmRoles());

        List<GroupRepresentation> subGroups = groupRepresentation.getSubGroups();
        applySubGroup(group, subGroups);

        return group;
    }

    private static void applySubGroup(Group group, List<GroupRepresentation> subGroups) {
    }
}
