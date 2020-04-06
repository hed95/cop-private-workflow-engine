package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import io.findify.s3mock.S3Mock
import org.apache.commons.io.IOUtils
import org.camunda.spin.Spin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService
import uk.gov.homeoffice.borders.workflow.process.ProcessStartDto

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CaseApiControllerSpec extends BaseSpec {

    @Autowired
    ProcessApplicationService applicationService

    @Autowired
    AmazonS3 amazonS3Client

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

    def 'can query cases'() {
        given:
        def user = logInUser()
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-555')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        applicationService.createInstance(processStartDto, user)._1()
        stubFor(post("/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&ignore_throttled=true&search_type=query_then_fetch&batched_reduce_size=512&ccs_minimize_roundtrips=true")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                            {
                                              "from" : 0,
                                              "size" : 20,
                                              "query" : {
                                                "query_string" : {
                                                  "query" : "BF-20200120*",
                                                  "fields" : [ ],
                                                  "type" : "best_fields",
                                                  "default_operator" : "or",
                                                  "max_determinized_states" : 10000,
                                                  "enable_position_increments" : true,
                                                  "fuzziness" : "AUTO",
                                                  "fuzzy_prefix_length" : 0,
                                                  "fuzzy_max_expansions" : 50,
                                                  "phrase_slop" : 0,
                                                  "escape" : false,
                                                  "auto_generate_synonyms_phrase_query" : true,
                                                  "fuzzy_transpositions" : true,
                                                  "boost" : 1.0
                                                }
                                              },
                                              "_source" : false
                                            }
                                            ''', true, true))

                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                        {
                                          "took" : 5,
                                          "timed_out" : false,
                                          "_shards" : {
                                            "total" : 1,
                                            "successful" : 1,
                                            "skipped" : 0,
                                            "failed" : 0
                                          },
                                          "hits" : {
                                            "total" : {
                                              "value" : 1,
                                              "relation" : "eq"
                                            },
                                            "max_score" : 1.3862942,
                                            "hits" : [
                                              {
                                                "_index" : "BF-20200120-555",
                                                "_type" : "_doc",
                                                "_id" : "0",
                                                "_score" : 1.3862942,
                                                "_source" : {
                                                  "businessKey" : "businessKey"
                                                }
                                              },
                                              {
                                                "_index" : "BF-20200120-551",
                                                "_type" : "_doc",
                                                "_id" : "0",
                                                "_score" : 1.3862942,
                                                "_source" : {
                                                  "businessKey" : "businessKey"
                                                }
                                              },
                                              {
                                                "_index" : "BF-20200120-522",
                                                "_type" : "_doc",
                                                "_id" : "0",
                                                "_score" : 1.3862942,
                                                "_source" : {
                                                  "businessKey" : "businessKey"
                                                }
                                              }
                                            ]
                                          }
                                        }
                                        """)))

        when:
        def result = mvc.perform(get("/api/workflow/cases?query=BF-20200120*"))

        then:
        result.andReturn().response.contentAsString != ''


    }

    def 'can get submission data for form'() {
        given:
        amazonS3Client.createBucket("test-events")

        def user = logInUser()
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-555')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.assignee ="test"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def response = applicationService.createInstance(processStartDto, user)
        def task = response._2().first()
        def processInstance = response._1()
        def definition = processInstance.getProcessDefinitionId()

        ObjectMetadata metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameA')
        metadata.addUserMetadata('title', 'formNameA')
        metadata.addUserMetadata('formversionid', 'formNameA')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', processInstance.id)

        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-555/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameB')
        metadata.addUserMetadata('title', 'formNameB')
        metadata.addUserMetadata('formversionid', 'formNameB')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', processInstance.id)

        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-555/peopleEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameC')
        metadata.addUserMetadata('title', 'formNameC')
        metadata.addUserMetadata('formversionid', 'formNameC')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-555/itemsEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameD')
        metadata.addUserMetadata('title', 'formNameD')
        metadata.addUserMetadata('formversionid', 'formNameD')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-555/journeyEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        when:
        def result = mvc.perform(get("/api/workflow/cases/BF-20200120-555"))

        then:
        result.andExpect(status().is2xxSuccessful())
        def caseDetail = Spin.JSON(result.andReturn().response.contentAsString);

        caseDetail.prop('businessKey').stringValue() == "BF-20200120-555"

        when:
        def submissionData = mvc.perform(get("/api/workflow/cases/BF-20200120-555/submission?key=BF-20200120-555/journeyEaB/20120101-xx@x.com.json"))

        then:
        def asSpin = Spin.JSON(IOUtils.toString(new ClassPathResource("data.json").getInputStream(), "UTF-8"))
        submissionData.andReturn().response.contentAsString == asSpin.toString()

    }
}
