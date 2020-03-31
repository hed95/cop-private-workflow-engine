package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class CaseConfig {

    @Autowired
    private AWSConfig awsConfig;


    @Bean
    @Primary
    public AmazonS3 awsS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsConfig.getCredentials().getAccessKey()
                , awsConfig.getCredentials().getSecretKey());
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsConfig.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }


}
