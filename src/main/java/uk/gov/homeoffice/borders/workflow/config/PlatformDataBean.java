package uk.gov.homeoffice.borders.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@ConfigurationProperties(prefix = "platform-data")
@Component
@Data
public class PlatformDataBean {

    private URI url;
    private int connectTimeout;
    private int readTimeout;
}
