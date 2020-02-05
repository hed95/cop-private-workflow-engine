package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import io.findify.s3mock.S3Mock
import org.camunda.bpm.engine.RepositoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import spock.lang.Shared
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
        def result = service.findBy('businessKey', PageRequest.of(0, 20))

        then:
        result.size() != 0
        result.content.get(0).associatedProcessInstances.size() == 1

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
        def result = service.findBy('BF-2020%', PageRequest.of(0, 20))

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
        def result = service.findBy('apples%', PageRequest.of(0, 20))

        then:
        result.size() == 0

    }

    def 'get case'() {
        given:
        amazonS3Client.createBucket("events")

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
        def processInstance = applicationService.createInstance(processStartDto, user)._1()
        def definition = processInstance.getProcessDefinitionId()

        ObjectMetadata metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameA')
        metadata.addUserMetadata('title', 'formNameA')
        metadata.addUserMetadata('formVersionId', 'formNameA')
        metadata.addUserMetadata('processDefinitionId', definition)

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameB')
        metadata.addUserMetadata('title', 'formNameB')
        metadata.addUserMetadata('formVersionId', 'formNameB')
        metadata.addUserMetadata('processDefinitionId', definition)

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/peopleEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameC')
        metadata.addUserMetadata('title', 'formNameC')
        metadata.addUserMetadata('formVersionId', 'formNameC')
        metadata.addUserMetadata('processDefinitionId', definition)
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/itemsEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameD')
        metadata.addUserMetadata('title', 'formNameD')
        metadata.addUserMetadata('formVersionId', 'formNameD')
        metadata.addUserMetadata('processDefinitionId', definition)
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/journeyEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        when:
        def caseDetails = service.getByKey('BF-20200120-555', user)

        then:
        caseDetails
        caseDetails.getProcessInstances().size() != 0
        caseDetails.getBusinessKey() == 'BF-20200120-555'
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
