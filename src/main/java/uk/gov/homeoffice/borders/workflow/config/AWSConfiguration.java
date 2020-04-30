package uk.gov.homeoffice.borders.workflow.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.homeoffice.borders.workflow.cases.AWSConfig;

@Configuration
@Profile("!test")
public class AWSConfiguration {


    private final AWSConfig awsConfig;

    public AWSConfiguration(AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
    }


    @Bean
    public AWSStaticCredentialsProvider credentials(){
        BasicAWSCredentials basicAWSCredentials =
                new BasicAWSCredentials(awsConfig.getCredentials().getAccessKey()
                , awsConfig.getCredentials().getSecretKey());
       return  new AWSStaticCredentialsProvider(basicAWSCredentials);

    }

    @Bean
    @Primary
    public AmazonS3 awsS3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsConfig.getRegion()))
                .withCredentials(credentials()).build();
    }

    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService() {
       return AmazonSimpleEmailServiceClientBuilder.standard()
               .withRegion(Regions.fromName(awsConfig.getRegion()))
               .withCredentials(credentials()).build();
    }

}
