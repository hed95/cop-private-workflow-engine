package uk.gov.homeoffice.borders.workflow.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.config.FormEngineRefBean;

@Configuration
public class FormEngineConfig {

    @Autowired
    private FormEngineRefBean formEngineRefBean;

    @Bean
    public FormEngineService formEngineService(RestTemplate restTemplate) {
        return new FormEngineService(formEngineRefBean, restTemplate);
    }

}
