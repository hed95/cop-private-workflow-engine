package uk.gov.homeoffice.borders.workflow.identity;


import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class IdentityConfig {

    @Value("${keycloak.auth-server-url}")
    private String authUrl;

    @Value("${keycloak-management.client-id}")
    private String keycloakManagementClientId;

    @Value("${keycloak-management.username}")
    private String keycloakManagementUsername;

    @Value("${keycloak-management.password}")
    private String keycloakManagementPassword;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public KeycloakIdentityProviderPlugin identityProviderPlugin() {
        return new KeycloakIdentityProviderPlugin(keycloakIdentityProviderFactory());
    }

    @Bean
    public UserService userService() {
        return new UserService(realmManagementKeycloak(), realm);
    }

    @Bean
    public GroupService groupService() {
        return new GroupService(realmManagementKeycloak(), realm);
    }


    @Bean
    public KeycloakIdentityProviderFactory keycloakIdentityProviderFactory() {
        return new KeycloakIdentityProviderFactory(keycloakIdentityProvider());
    }

    @Bean
    public KeycloakIdentityProvider keycloakIdentityProvider() {
        return new KeycloakIdentityProvider(userService(), groupService());
    }
    @Bean
    public Keycloak realmManagementKeycloak() {
        return KeycloakBuilder.builder()
                .clientId(keycloakManagementClientId)
                .username(keycloakManagementUsername)
                .password(keycloakManagementPassword)
                .serverUrl(authUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
    }
}
