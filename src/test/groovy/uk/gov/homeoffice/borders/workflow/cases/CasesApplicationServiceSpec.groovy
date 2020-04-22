package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.github.tomakehurst.wiremock.http.Fault
import io.findify.s3mock.S3Mock
import org.apache.commons.io.IOUtils
import org.camunda.bpm.engine.RepositoryService
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService
import uk.gov.homeoffice.borders.workflow.process.ProcessStartDto
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor


class CasesApplicationServiceSpec extends BaseSpec {

    @Autowired
    CasesApplicationService service

    @Autowired
    ProcessApplicationService applicationService

    @Autowired
    RepositoryService repositoryService


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


    def 'can get cases for business key'() {
        given:
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20120012-222')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        applicationService.createInstance(processStartDto, user)

        and:
        stubFor(post("/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&ignore_throttled=true&search_type=query_then_fetch&batched_reduce_size=512&ccs_minimize_roundtrips=true")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                           {
                                              "from" : 0,
                                              "size" : 20,
                                              "query" : {
                                                "query_string" : {
                                                  "query" : "apples",
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
                                              "_source":{"includes":["businessKey"],"excludes":[]}}
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
                                                "_index" : "bf-20120012-222",
                                                "_type" : "_doc",
                                                "_id" : "0",
                                                "_score" : 1.3862942,
                                                "_source" : {
                                                  "businessKey" : "BF-20120012-222"
                                                }
                                              }
                                            ]
                                          }
                                        }
                                        """)))


        when:
        def result = service.query('apples', PageRequest.of(0, 20), user)

        then:
        result.size() != 0

    }

    def 'empty collection if ES failed'() {
        given:
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('businessKey')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        applicationService.createInstance(processStartDto, user)

        and:
        stubFor(post("/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&ignore_throttled=true&search_type=query_then_fetch&batched_reduce_size=512&ccs_minimize_roundtrips=true")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                                {
                                                  "from" : 0,
                                                  "size" : 20,
                                                  "query" : {
                                                    "query_string" : {
                                                      "query" : "apples",
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
                                                   "_source":{"includes":["businessKey"],"excludes":[]}}
                                                }
                                            ''', true, true))
                .willReturn(aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)
                        .withHeader("Content-Type", "application/json")))


        when:
        def result = service.query('apples', PageRequest.of(0, 20), user)

        then:
        result.size() == 0

    }

    def 'can get cases for partial business key'() {
        given:
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
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        applicationService.createInstance(processStartDto, user)

        and:
        stubFor(post("/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&ignore_throttled=true&search_type=query_then_fetch&batched_reduce_size=512&ccs_minimize_roundtrips=true")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                            {
                                                  "from" : 0,
                                                  "size" : 20,
                                                  "query" : {
                                                    "query_string" : {
                                                      "query" : "BF-2020*",
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
                                                   "_source":{"includes":["businessKey"],"excludes":[]}}
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
                                                  "businessKey" : "BF-20200120-555"
                                                }
                                              }
                                            ]
                                          }
                                        }
                                        """)))

        when:
        def result = service.query('BF-2020*', PageRequest.of(0, 20), user)

        then:
        result.size() != 0


    }

    def 'no cases returned for non business key'() {
        given:
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
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        applicationService.createInstance(processStartDto, user)
        and:
        stubFor(post("/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&ignore_throttled=true&search_type=query_then_fetch&batched_reduce_size=512&ccs_minimize_roundtrips=true")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                            {
                                                  "from" : 0,
                                                  "size" : 20,
                                                  "query" : {
                                                    "query_string" : {
                                                      "query" : "apples*",
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
                                                   "_source":{"includes":["businessKey"],"excludes":[]}}
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
                                              "value" : 0,
                                              "relation" : "eq"
                                            },
                                            "max_score" : 1.3862942,
                                            "hits" : []
                                          }
                                        }
                                        """)))

        when:
        def result = service.query('apples*', PageRequest.of(0, 20), user)

        then:
        result.size() == 0

    }


    def 'get case does not return process instance if user is not candidateGroup role'() {
        given:
        amazonS3Client.createBucket("test-events")

        def user = logInUser()
        user.roles = ['different_role']
        user.shiftDetails.roles = ['different_role']

        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-001')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.assignee ="test"
        data.description = "test 0"
        data.form ='''{
            "shiftDetailsContext": {
              "email" : "email
            },
            "form": {
               "name": "name",
               "title" : "title",
               "formVersionId": "formVersionId"
            }
            
        }'''
        processStartDto.data = [data]
        processStartDto

        and:
        def result = applicationService.createInstance(processStartDto, user)


        when:
        def caseDetails = service.getByKey('BF-20200120-001', [] , user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() == 0

    }
    def 'get case returns process instance if user is candidateUser'() {
        given:
        amazonS3Client.createBucket("test-events")

        def user = logInUser()
        user.roles = ['different_role']
        user.shiftDetails.roles = ['different_role']

        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'testProcessCandidateUser'
        processStartDto.variableName = 'data'
        processStartDto.setBusinessKey('BF-20200120-002')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.assignee ="test"
        data.description = "test 0"
        processStartDto.data = data
        processStartDto

        and:
        applicationService.createInstance(processStartDto, user)


        when:
        def caseDetails = service.getByKey('BF-20200120-002',[], user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() != 0

    }

    def 'can exclude process key'() {
        given:
        amazonS3Client.createBucket("test-events")

        def user = logInUser()
        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-001')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.assignee ="test"
        data.description = "test 0"
        data.form = '''{
                        "test": "test",
                        "form" : {
                           "test": "test"
                         } 
                       }'''
        processStartDto.data = [data]
        processStartDto

        and:
        applicationService.createInstance(processStartDto, user)

        when:
        def result = service.getByKey('BF-20200120-001', ['encryption'], user)

        then:
        result.processInstances.size() == 0

    }
    def 'get case'() {
        given:
        amazonS3Client.createBucket("test-events")

        def user = logInUser()
        def token = new TestingAuthenticationToken(user, "test")
        token.setAuthenticated(true)
        SecurityContextHolder.setContext(new SecurityContextImpl(token))

        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-000')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.assignee ="test"
        data.description = "test 0"
        data.form = '''{
                        "test": "test",
                        "form" : {
                           "test": "test"
                         } 
                       }'''
        processStartDto.data = [data]
        processStartDto

        and:
        def result = applicationService.createInstance(processStartDto, user)
        def task = result._2().first()
        def processInstance = result._1()
        def definition = processInstance.getProcessDefinitionId()

        ObjectMetadata metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameA')
        metadata.addUserMetadata('title', 'formNameA')
        metadata.addUserMetadata('formversionid', 'formNameA')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', processInstance.id)

        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-000/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameB')
        metadata.addUserMetadata('title', 'formNameB')
        metadata.addUserMetadata('formversionid', 'formNameB')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', processInstance.id)

        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-000/peopleEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameC')
        metadata.addUserMetadata('title', 'formNameC')
        metadata.addUserMetadata('formversionid', 'formNameC')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-000/itemsEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameD')
        metadata.addUserMetadata('title', 'formNameD')
        metadata.addUserMetadata('formversionid', 'formNameD')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("test-events", "BF-20200120-000/journeyEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        taskService.complete(task.id)
        runtimeService.createMessageCorrelation('waiting').processInstanceId(processInstance.id).correlate()

        when:
        def caseDetails = service.getByKey('BF-20200120-000', [], user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() != 0
        caseDetails.getProcessInstances().first().getFormReferences().size() == 2
        caseDetails.getBusinessKey() == 'BF-20200120-000'
        caseDetails.getActions().size() != 0
        caseDetails.getMetrics() != null

        when:
        SpinJsonNode submissionData = service
                .getSubmissionData('BF-20200120-000', "BF-20200120-000/journeyEaB/20120101-xx@x.com.json", user)

        then:
        def asSpin = Spin.JSON(IOUtils.toString(new ClassPathResource("data.json").getInputStream(), "UTF-8"))
        submissionData.toString() == asSpin.toString()
    }

    ProcessStartDto createProcessStartDto() {
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'test'
        processStartDto.variableName = 'collectionOfData'
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        data.assignee = "assigneeOneTwoThree"
        processStartDto.data = [data]
        processStartDto
    }

    PlatformUser logInUser() {
        def user = new PlatformUser()
        user.id = 'test'
        user.email = 'test'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user
    }

}
