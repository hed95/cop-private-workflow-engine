package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.time.LocalDateTime;

@Configuration
@Profile("!test")
public class CaseConfig {

    private final AWSConfig awsConfig;

    public CaseConfig(AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
    }


    @Bean
    @Primary
    public AmazonS3 awsS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsConfig.getCredentials().getAccessKey()
                , awsConfig.getCredentials().getSecretKey());
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsConfig.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }


    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {

        AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getCredentials().getAccessKey()
                , awsConfig.getCredentials().getSecretKey());

        final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AWSSigner signer = new AWSSigner(credentialsProvider, awsConfig.getAwsElasticSearch().getRegion(),
                "workflow-engine", LocalDateTime::now);

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(
                        awsConfig.getAwsElasticSearch().getUrl()
                )).setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.addInterceptorLast(new AWSSigningRequestInterceptor(signer))));

    }


}
