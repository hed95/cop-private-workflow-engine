package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest

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

        when:
        def result = service.queryByKey('businessKey', PageRequest.of(0, 20), user)

        then:
        result.size() != 0

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

        when:
        def result = service.queryByKey('BF-2020%', PageRequest.of(0, 20), user)

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

        when:
        def result = service.queryByKey('apples%', PageRequest.of(0, 20), user)

        then:
        result.size() == 0

    }


    def 'get case does not return process instance if user is not candidateGroup role'() {
        given:
        amazonS3Client.createBucket("events")

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
        processStartDto.data = [data]
        processStartDto

        and:
        def result = applicationService.createInstance(processStartDto, user)


        when:
        def caseDetails = service.getByKey('BF-20200120-001', user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() == 0

    }
    def 'get case returns process instance if user is candidateUser'() {
        given:
        amazonS3Client.createBucket("events")

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
        def caseDetails = service.getByKey('BF-20200120-002', user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() != 0

    }

    def 'get case'() {
        given:
        amazonS3Client.createBucket("events")

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

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-000/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameB')
        metadata.addUserMetadata('title', 'formNameB')
        metadata.addUserMetadata('formversionid', 'formNameB')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', processInstance.id)

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-000/peopleEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameC')
        metadata.addUserMetadata('title', 'formNameC')
        metadata.addUserMetadata('formversionid', 'formNameC')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-000/itemsEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameD')
        metadata.addUserMetadata('title', 'formNameD')
        metadata.addUserMetadata('formversionid', 'formNameD')
        metadata.addUserMetadata('processdefinitionid', definition)
        metadata.addUserMetadata('processinstanceid', 'processinstanceidB')
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-000/journeyEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        taskService.complete(task.id)
        runtimeService.createMessageCorrelation('waiting').processInstanceId(processInstance.id).correlate()

        when:
        def caseDetails = service.getByKey('BF-20200120-000', user)

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
