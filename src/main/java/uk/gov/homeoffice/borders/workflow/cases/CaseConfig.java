package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.auth.AWS4Signer;
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


@Configuration
@Profile("!test")
public class CaseConfig {

    private final AWSConfig awsConfig;

    public CaseConfig(AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
    }


    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {

        AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getCredentials().getAccessKey()
                , awsConfig.getCredentials().getSecretKey());

        final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(awsConfig.getElasticSearch().getRegion());
        signer.setServiceName("es");

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(
                        awsConfig.getElasticSearch().getEndpoint(), 443, "https"
                )).setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.addInterceptorFirst(new AWSRequestSigningApacheInterceptor("es",
                                signer, credentialsProvider)
                        )
                )

        );
    }



}
