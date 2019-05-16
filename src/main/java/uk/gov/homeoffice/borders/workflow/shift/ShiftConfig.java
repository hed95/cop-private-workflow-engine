package uk.gov.homeoffice.borders.workflow.shift;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.config.PlatformDataBean;

@Configuration
public class ShiftConfig {

    @Autowired
    private PlatformDataBean platformDataBean;

    @Bean
    public ShiftApplicationService shiftApplicationService(RuntimeService runtimeService,
                                                           PlatformDataUrlBuilder platformDataUrlBuilder,
                                                           RefDataUrlBuilder refDataUrlBuilder,
                                                           JacksonJsonDataFormat formatter, RestTemplate restTemplate) {
        return new ShiftApplicationService(runtimeService, restTemplate,
                platformDataUrlBuilder,
                refDataUrlBuilder, formatter);
    }


}
