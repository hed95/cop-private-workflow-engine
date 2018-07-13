package uk.gov.homeoffice.borders.workflow

import org.camunda.bpm.engine.IdentityService
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication

class RestApiUserExtractorSpec extends Specification {

    def identityService = Mock(IdentityService)

    def restApiUserExtractor = new RestApiUserExtractor(identityService)

    def 'can get user'() {
        given:
        def user = new ShiftUser()
        user.id ='id'
        user.email = 'email'
        user.teams = []
        def workflowAuthentication = new WorkflowAuthentication(user)
        identityService.getCurrentAuthentication() >> workflowAuthentication

        when:
        def result = restApiUserExtractor.toUser()

        then:
        result
        result == user
    }

    def 'exception thrown if no user found'() {
        given:
        def workflowAuthentication = new WorkflowAuthentication("test", [])
        identityService.getCurrentAuthentication() >> workflowAuthentication

        when:
        restApiUserExtractor.toUser()

        then:
        thrown ForbiddenException
    }

    def 'exception thrown if no authentication found'() {
        given:
        identityService.getCurrentAuthentication() >> null

        when:
        restApiUserExtractor.toUser()

        then:
        thrown ForbiddenException
    }
}
