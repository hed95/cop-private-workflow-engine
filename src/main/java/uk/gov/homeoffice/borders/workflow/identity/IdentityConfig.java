package uk.gov.homeoffice.borders.workflow.identity;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${prest-url}")
    private String prestUrl;


    @Bean
    public CustomIdentityProviderPlugin identityProviderPlugin() {
        return new CustomIdentityProviderPlugin(customIdentityProviderFactory());
    }

    @Bean
    public UserService userService() {
        return new UserService(restTemplate, prestUrl, objectMapper);
    }


    @Bean
    public TeamService teamService() {
        return new TeamService(restTemplate, prestUrl, objectMapper);
    }

    @Bean
    public CustomIdentityProviderFactory customIdentityProviderFactory() {
        return new CustomIdentityProviderFactory(customIdentityProvider());
    }

    @Bean
    public CustomIdentityProvider customIdentityProvider() {
        return new CustomIdentityProvider(userService(), teamService());
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
