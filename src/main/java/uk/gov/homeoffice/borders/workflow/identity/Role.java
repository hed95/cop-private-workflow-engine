package uk.gov.homeoffice.borders.workflow.identity;

import lombok.Data;
import org.keycloak.representations.idm.RoleRepresentation;

@Data
public class Role implements org.camunda.bpm.engine.identity.Group {

    private String id;
    private String name;
    private String description;
    private String type;


    public static Role toRole(RoleRepresentation roleRepresentation) {
        Role group = new Role();

        group.setId(roleRepresentation.getId());
        group.setName(roleRepresentation.getName());
        group.setDescription(roleRepresentation.getDescription());
        group.setType("");

        return group;
    }

}
