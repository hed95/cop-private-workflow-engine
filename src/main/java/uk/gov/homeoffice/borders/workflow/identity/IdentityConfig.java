package uk.gov.homeoffice.borders.workflow.identity;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;

@Configuration
public class IdentityConfig {

    @Autowired
    private PlatformDataUrlBuilder platformDataUrlBuilder;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public CustomIdentityProviderPlugin identityProviderPlugin() {
        return new CustomIdentityProviderPlugin(customIdentityProviderFactory());
    }

    @Bean
    public UserService userService() {
        return new UserService(restTemplate, platformDataUrlBuilder, teamService());
    }


    @Bean
    public TeamService teamService() {
        return new TeamService(restTemplate, platformDataUrlBuilder);
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
