package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.amazonaws.http.HttpMethodName;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.http.protocol.HttpCoreContext.HTTP_TARGET_HOST;


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
        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(awsConfig.getElasticSearch().getRegion());
        signer.setServiceName("es");

        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(
                        awsConfig.getElasticSearch().getEndpoint(), 443, "https"
                )).setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.addInterceptorFirst(new AWSRequestSigningApacheInterceptor("es",
                                signer, credentialsProvider)
                        )
                )

        );
        return restHighLevelClient;
    }



    /**
     * An {@link HttpRequestInterceptor} that signs requests using any AWS {@link Signer}
     * and {@link AWSCredentialsProvider}.
     */
    public static class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
        /**
         * The service that we're connecting to. Technically not necessary.
         * Could be used by a future Signer, though.
         */
        private final String service;

        /**
         * The particular signer implementation.
         */
        private final Signer signer;

        /**
         * The source of AWS credentials for signing.
         */
        private final AWSCredentialsProvider awsCredentialsProvider;

        /**
         *
         * @param service service that we're connecting to
         * @param signer particular signer implementation
         * @param awsCredentialsProvider source of AWS credentials for signing
         */
        public AWSRequestSigningApacheInterceptor(final String service,
                                                  final Signer signer,
                                                  final AWSCredentialsProvider awsCredentialsProvider) {
            this.service = service;
            this.signer = signer;
            this.awsCredentialsProvider = awsCredentialsProvider;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {
            URIBuilder uriBuilder;
            try {
                uriBuilder = new URIBuilder(request.getRequestLine().getUri());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI" , e);
            }

            // Copy Apache HttpRequest to AWS DefaultRequest
            DefaultRequest<?> signableRequest = new DefaultRequest<>(service);

            HttpHost host = (HttpHost) context.getAttribute(HTTP_TARGET_HOST);
            if (host != null) {
                signableRequest.setEndpoint(URI.create(host.toURI()));
            }
            final HttpMethodName httpMethod =
                    HttpMethodName.fromValue(request.getRequestLine().getMethod());
            signableRequest.setHttpMethod(httpMethod);
            try {
                signableRequest.setResourcePath(uriBuilder.build().getRawPath());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI" , e);
            }

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                        (HttpEntityEnclosingRequest) request;
                if (httpEntityEnclosingRequest.getEntity() != null) {
                    signableRequest.setContent(httpEntityEnclosingRequest.getEntity().getContent());
                }
            }
            signableRequest.setParameters(nvpToMapParams(uriBuilder.getQueryParams()));
            signableRequest.setHeaders(headerArrayToMap(request.getAllHeaders()));

            // Sign it
            signer.sign(signableRequest, awsCredentialsProvider.getCredentials());

            // Now copy everything back
            request.setHeaders(mapToHeaderArray(signableRequest.getHeaders()));
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                        (HttpEntityEnclosingRequest) request;
                if (httpEntityEnclosingRequest.getEntity() != null) {
                    BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                    basicHttpEntity.setContent(signableRequest.getContent());
                    httpEntityEnclosingRequest.setEntity(basicHttpEntity);
                }
            }
        }

        /**
         *
         * @param params list of HTTP query params as NameValuePairs
         * @return a multimap of HTTP query params
         */
        private static Map<String, List<String>> nvpToMapParams(final List<NameValuePair> params) {
            Map<String, List<String>> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (NameValuePair nvp : params) {
                List<String> argsList =
                        parameterMap.computeIfAbsent(nvp.getName(), k -> new ArrayList<>());
                argsList.add(nvp.getValue());
            }
            return parameterMap;
        }

        /**
         * @param headers modeled Header objects
         * @return a Map of header entries
         */
        private static Map<String, String> headerArrayToMap(final Header[] headers) {
            Map<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Header header : headers) {
                if (!skipHeader(header)) {
                    headersMap.put(header.getName(), header.getValue());
                }
            }
            return headersMap;
        }

        /**
         * @param header header line to check
         * @return true if the given header should be excluded when signing
         */
        private static boolean skipHeader(final Header header) {
            return ("content-length".equalsIgnoreCase(header.getName())
                    && "0".equals(header.getValue())) // Strip Content-Length: 0
                    || "host".equalsIgnoreCase(header.getName()); // Host comes from endpoint
        }

        /**
         * @param mapHeaders Map of header entries
         * @return modeled Header objects
         */
        private static Header[] mapToHeaderArray(final Map<String, String> mapHeaders) {
            Header[] headers = new Header[mapHeaders.size()];
            int i = 0;
            for (Map.Entry<String, String> headerEntry : mapHeaders.entrySet()) {
                headers[i++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue());
            }
            return headers;
        }
    }


}
