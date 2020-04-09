package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CaseReIndexAuthorizationChecker {

    @Value("#{T(java.util.Arrays).asList('${engine.admin.roles:}')}")
    private List<String> engineAdminRoles = new ArrayList<>();

    public boolean isAuthorized(Authentication authentication) {
        if (engineAdminRoles.isEmpty()) {
            return true;
        }

        boolean isAllowed =  getRoles(authentication).stream().anyMatch(engineAdminRoles::contains);
        log.info("User '{}' is allowed to reindex {}", authentication.getPrincipal(), isAllowed);
        return isAllowed;
    }

    private List<String> getRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
