package uk.gov.homeoffice.borders.workflow.process

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication

import javax.crypto.SealedObject

class ProcessApplicationServiceSpec extends BaseSpec {

    @Autowired
    ProcessApplicationService applicationService

    def 'can encrypt variables'() {
        given:
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
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

        when:
        def processInstance = applicationService.createInstance(processStartDto, user)
        def result = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.id)
                .variableName('collectionOfData').singleResult()


        then:
        result.value instanceof SealedObject



    }
}
