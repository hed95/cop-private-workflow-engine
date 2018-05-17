package uk.gov.homeoffice.borders.workflow.shift;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;

@Configuration
public class ShiftConfig {

    @Value("${platform-data-token}")
    private String platformDataToken;

    @Bean
    public ShiftApplicationService shiftApplicationService(RuntimeService runtimeService,
                                                           PlatformDataUrlBuilder platformDataUrlBuilder) {
        return new ShiftApplicationService(runtimeService, new RestTemplate(), platformDataUrlBuilder, platformDataToken);
    }
}
