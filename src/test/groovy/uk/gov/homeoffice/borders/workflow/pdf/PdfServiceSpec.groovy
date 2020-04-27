package uk.gov.homeoffice.borders.workflow.pdf

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.github.tomakehurst.wiremock.client.WireMock
import io.findify.s3mock.S3Mock
import org.camunda.bpm.engine.ProcessEngineException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService
import uk.gov.homeoffice.borders.workflow.process.ProcessStartDto

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.http.Response.response

class PdfServiceSpec extends BaseSpec {

    @Autowired
    AmazonS3 amazonS3Client

    @Autowired
    PdfService pdfService

    @Autowired
    ProcessApplicationService applicationService

    static S3Mock api = new S3Mock.Builder().withPort(8323).withInMemoryBackend().build()


    def setupSpec() {
        if (api != null) {
            api.start()
        }
    }

    def cleanupSpec() {
        if (api != null) {
            api.shutdown()
        }
    }


    def 'can make a request to generate pdf'() {
        given:
        amazonS3Client.createBucket("test-cop-case")

        def user = logInUser()
        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        and:
        amazonS3Client.putObject(new PutObjectRequest("test-cop-case", "BF-20200120-000/formEaB/xx@x.com-20200128T083155.json",
                new ClassPathResource("data.json").getInputStream(), new ObjectMetadata()))


        and:
        wireMockStub.stub {
            request {
                method 'POST'
                url '/pdf'
            }
            response {
                status: 200
                headers {
                    "Content-Type" "application/json"
                }
            }
        }


        when:
        def processDto = new ProcessStartDto()
        processDto.setBusinessKey("BF-20200120-000")
        processDto.setProcessKey("generatePDFExample")
        processDto.setVariableName("exampleForm")
        processDto.setData('''{
            "test" : "test",
            "businessKey": "BF-20200120-000",
            "form" : {
               "name" : "formEaB",
               "submittedBy": "xx@x.com",
               "submissionDate": "2020-01-28T08:31:55.297Z",
               "versionId": "formVersionId"
            }
        }''')

        def result = applicationService.createInstance(processDto, user)

        then:
        result


    }

    def 'raises an incident if pdf request fails'() {
        given:
        amazonS3Client.createBucket("test-cop-case")

        def user = logInUser()
        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        and:
        amazonS3Client.putObject(new PutObjectRequest("test-cop-case", "BF-20200120-000/formEaB/xx@x.com-20200128T083155.json",
                new ClassPathResource("data.json").getInputStream(), new ObjectMetadata()))


        and:
        wireMockRule.stubFor(WireMock.post(WireMock.urlMatching("/pdf")).willReturn(
                aResponse().withStatus(500))
        )

        when:
        def processDto = new ProcessStartDto()
        processDto.setBusinessKey("BF-20200120-000")
        processDto.setProcessKey("generatePDFExample")
        processDto.setVariableName("exampleForm")
        processDto.setData('''{
            "test" : "test",
            "businessKey": "BF-20200120-000",
            "form" : {
               "name" : "formEaB",
               "submittedBy": "xx@x.com",
               "submissionDate": "2020-01-28T08:31:55.297Z",
               "versionId": "formVersionId"
            }
        }''')
        applicationService.createInstance(processDto, user)

        then:
        ProcessEngineException exception = thrown()
        exception
    }
}
