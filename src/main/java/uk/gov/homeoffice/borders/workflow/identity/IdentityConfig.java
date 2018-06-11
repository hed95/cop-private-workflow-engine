package uk.gov.homeoffice.borders.workflow.identity;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;

@Configuration
public class IdentityConfig {

    private RestTemplate platformDataRestTemplate = new RestTemplate();

    @Autowired
    private PlatformDataUrlBuilder platformDataUrlBuilder;


    @Bean
    public CustomIdentityProviderPlugin identityProviderPlugin() {
        return new CustomIdentityProviderPlugin(customIdentityProviderFactory());
    }

    @Bean
    public UserService userService() {
        return new UserService(platformDataRestTemplate, platformDataUrlBuilder);
    }


    @Bean
    public TeamService teamService() {
        return new TeamService(platformDataRestTemplate, platformDataUrlBuilder);
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
