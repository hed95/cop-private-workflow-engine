package uk.gov.homeoffice.borders.workflow.resource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
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


        @Bean
        public AmazonS3 amazonS3Client() {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            return AmazonS3ClientBuilder.standard()
                    .withRegion("eu-west-2")
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
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
