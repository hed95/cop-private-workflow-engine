package uk.gov.homeoffice.borders.workflow.shift;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.config.PlatformDataBean;

import java.util.List;

@Configuration
public class ShiftConfig {

    @Autowired
    private PlatformDataBean platformDataBean;

    @Bean
    public ShiftApplicationService shiftApplicationService(RuntimeService runtimeService,
                                                           PlatformDataUrlBuilder platformDataUrlBuilder,
                                                           JacksonJsonDataFormat formatter) {
        return new ShiftApplicationService(runtimeService, new RestTemplate(), platformDataUrlBuilder,
                platformDataBean, formatter);
    }


    @Configuration
    public static class ShiftUserMethodConfig implements WebMvcConfigurer {

        @Autowired
        private IdentityService identityService;

        @Override
        public void addArgumentResolvers(
                List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new ShiftUserMethodArgumentResolver(identityService));
        }
    }
}
