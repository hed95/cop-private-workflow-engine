package uk.gov.homeoffice.borders.workflow.identity;


import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.shift.ShiftApplicationService;

@Configuration
public class IdentityConfig {

    @Autowired
    private PlatformDataUrlBuilder platformDataUrlBuilder;
    @Autowired
    private RefDataUrlBuilder refDataUrlBuilder;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public CustomIdentityProviderPlugin identityProviderPlugin(@Lazy ShiftApplicationService shiftApplicationService) {
        return new CustomIdentityProviderPlugin(new CustomIdentityProviderFactory(new CustomIdentityProvider(userService(shiftApplicationService), teamService())));
    }

    @Bean
    public UserService userService(@Lazy ShiftApplicationService shiftApplicationService) {
        return new UserService(restTemplate, platformDataUrlBuilder, refDataUrlBuilder, shiftApplicationService);
    }


    @Bean
    public TeamService teamService() {
        return new TeamService(restTemplate, refDataUrlBuilder);
    }

}
