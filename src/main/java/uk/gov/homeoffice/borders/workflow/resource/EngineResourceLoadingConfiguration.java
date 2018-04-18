package uk.gov.homeoffice.borders.workflow.resource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class EngineResourceLoadingConfiguration {


    @Configuration
    @Profile({"dev", "prod"})
    public static class S3EngineResourceConfiguration {

        @Value("${engine.resource.s3.bucketName}")
        private String bucketName;
        @Value("${engine.resource.s3.accessKey}")
        private String accessKey;
        @Value("${engine.resource.s3.secretKey}")
        private String secretKey;
        @Value("${engine.resource.s3.endpoint:default}")
        private String s3Endpoint;
        @Value("${engine.resource.s3.region:eu-west-2}")
        private String region;

        @Bean
        public AmazonS3 amazonS3Client() {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            if (s3Endpoint.equalsIgnoreCase("default")) {
                log.info("Using default configuration....");
                return AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

            } else {
                log.info("Using S3 endpoint configuration....");
                return AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(s3Endpoint, region)
                        ).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

            }

        }


        @Bean
        public EngineResourceLoader engineResourceLoader() {
            return new AmazonS3EngineResourceLoader(amazonS3Client(), bucketName);
        }
    }

    @Configuration
    @Profile({"local", "test"})
    public static class ClassPathResourceConfiguration {

        @Value("${engine.resource.location}")
        private String classPathLocation;

        @Bean
        public EngineResourceLoader engineResourceLoader() {
            return new FileBasedEngineResourceLoader(classPathLocation);
        }
    }
}
