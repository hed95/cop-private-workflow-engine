package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RoleService {

    private Keycloak keycloak;
    private String realm;

    public Role findById(String roleId) {
        RoleRepresentation groupRepresentation = keycloak.realm(realm).roles().get(roleId).toRepresentation();
        return Role.toRole(groupRepresentation);
    }


    public List<Role> allRoles() {
        return keycloak.realm(realm).roles().list().stream()
                .map(Role::toRole).collect(toList());
    }
}
