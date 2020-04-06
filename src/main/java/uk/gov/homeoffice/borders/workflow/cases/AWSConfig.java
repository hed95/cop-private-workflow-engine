package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aws")
@Component
@Data
public class AWSConfig {

    private String region;
    private String caseBucketName;
    private Credentials credentials;
    private ElasticSearch elasticSearch;


    @Data
    public static class ElasticSearch {
        private String region;
        private String endpoint;
        private Credentials credentials;
    }

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }
}
