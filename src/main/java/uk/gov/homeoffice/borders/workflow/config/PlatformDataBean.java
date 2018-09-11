package uk.gov.homeoffice.borders.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "platform-data")
@Component
@Data
public class PlatformDataBean {

    private String url;
    private String token;
}
