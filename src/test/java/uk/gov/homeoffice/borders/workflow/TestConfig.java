package uk.gov.homeoffice.borders.workflow;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Primary
    public RestTemplate keycloakRestTemplate() {
        return new RestTemplate();
    }
}