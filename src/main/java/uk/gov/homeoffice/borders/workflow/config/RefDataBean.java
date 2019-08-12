package uk.gov.homeoffice.borders.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@ConfigurationProperties(prefix = "api-ref")
@Component
@Data
public class RefDataBean {

    private URI url;
}
