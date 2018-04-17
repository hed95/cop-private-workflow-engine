package uk.gov.homeoffice.borders.workflow.identity;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
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


    @Autowired
    private KeycloakRestTemplate restTemplate;

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
        return new UserService(prestUrl,objectMapper, restTemplate);
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

}
