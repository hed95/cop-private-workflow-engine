package uk.gov.homeoffice.borders.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@ConfigurationProperties(prefix = "private-ui")
@Component
@Data
public class PrivateUiBean {
    private URI url;

}
