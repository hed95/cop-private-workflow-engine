package uk.gov.homeoffice.borders.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@ConfigurationProperties(prefix = "form-api")
@Component
@Data
public class FormEngineRefBean {

    private URI url;
    private int connectTimeout;
    private int readTimeout;
}
