package uk.gov.homeoffice.borders.workflow.event

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.common.base.Supplier
import org.apache.http.HttpHost
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification
import vc.inreach.aws.request.AWSSigner
import vc.inreach.aws.request.AWSSigningRequestInterceptor

import java.time.LocalDateTime

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static java.time.LocalDateTime.now

class FormToAWSESUploaderSpec extends Specification {

    def static wmPort = 8010

    @ClassRule
    @Shared
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    private FormToAWSESUploader uploader
    private RestHighLevelClient restHighLevelClient
    private RuntimeService runtimeService

    def setup() {
        runtimeService = Mock()
        final BasicAWSCredentials credentials = new BasicAWSCredentials("accessKey", "secretAccessKey")

        final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials)
        AWSSigner signer = new AWSSigner(credentialsProvider, 'eu-west-2', "workflow-engine",
                new Supplier<LocalDateTime>() {
                    @Override
                    LocalDateTime get() {
                        return now()
                    }
                })

        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(
                        "http://127.0.01:8010"
                )).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.addInterceptorLast(new AWSSigningRequestInterceptor(signer))
                    }
                }))

        uploader = new FormToAWSESUploader(restHighLevelClient, runtimeService)
    }


    def cleanup() {
        if (restHighLevelClient != null) {
            restHighLevelClient.close()
        }
    }

    def 'can upload to ES'() {
        given: 'form data'
        def form = '''{
                        "test": "test" 
                      }'''

        ProcessInstance processInstance = Mock()
        processInstance.getBusinessKey() >> 'businessKey'

        and:
        stubFor(put("/businessKey/_doc/%2FbusinessKey%2FtestForm%2Femail%2F29129121.json?timeout=1m")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                            {
                                             "test": "test"
                                            }
                                            ''', true, true))

                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                        {
                                          "_index" : "businessKey",
                                          "_type" : "_doc",
                                          "_id" : "/businessKey/testForm/email/29129121.json",
                                          "_version" : 1,
                                          "result" : "created",
                                          "_shards" : {
                                            "total" : 2,
                                            "successful" : 2,
                                            "failed" : 0
                                          },
                                          "_seq_no" : 26,
                                          "_primary_term" : 4
                                        }
                                        """)))

        when:
        uploader.upload(form, "/businessKey/testForm/email/29129121.json", processInstance, 'executionId')

        then:
        true
    }
}
