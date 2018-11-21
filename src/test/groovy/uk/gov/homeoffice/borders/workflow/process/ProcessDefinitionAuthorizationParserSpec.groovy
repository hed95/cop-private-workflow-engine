package uk.gov.homeoffice.borders.workflow.process

import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.authorization.AuthorizationQuery
import org.camunda.bpm.engine.authorization.Resources
import org.camunda.bpm.engine.impl.el.FixedValue
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionStatisticsEntity
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException

class ProcessDefinitionAuthorizationParserSpec extends Specification {

    def authorizationService = Mock(AuthorizationService)
    def underTest = new ProcessDefinitionAuthorizationParser(authorizationService)


    def 'can record authorization for process model'() {
        given:
        def processDefinitionEntity = new ProcessDefinitionStatisticsEntity()
        processDefinitionEntity.key = 'processDefinitionKey'
        processDefinitionEntity.candidateStarterGroupIdExpressions = [new FixedValue("custom_role")]
        def query = Mock(AuthorizationQuery)

        and:
        authorizationService.createAuthorizationQuery() >> query
        query.resourceId('processDefinitionKey') >> query
        query.resourceType(Resources.PROCESS_DEFINITION) >> query
        query.singleResult() >> null

        and:
        def authorization = new AuthorizationEntity(Authorization.AUTH_TYPE_GRANT)
        authorization.id = 'id'
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT) >> authorization

        when:
        underTest.parseProcess(processDefinitionEntity)

        then:
        1 * authorizationService.saveAuthorization(authorization)
    }


    def 'delete previous authorizations'() {
        given:
        def processDefinitionEntity = new ProcessDefinitionStatisticsEntity()
        processDefinitionEntity.key = 'processDefinitionKey'
        processDefinitionEntity.candidateStarterGroupIdExpressions = [new FixedValue("custom_role")]
        def query = Mock(AuthorizationQuery)

        and:
        def previousAuthorization = new AuthorizationEntity(Authorization.AUTH_TYPE_GRANT)
        previousAuthorization.id = 'previousId'
        authorizationService.createAuthorizationQuery() >> query
        query.resourceId('processDefinitionKey') >> query
        query.resourceType(Resources.PROCESS_DEFINITION) >> query
        query.singleResult() >> previousAuthorization

        and:
        def authorization = new AuthorizationEntity(Authorization.AUTH_TYPE_GRANT)
        authorization.id = 'id'
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT) >> authorization

        when:
        underTest.parseProcess(processDefinitionEntity)

        then:
        1 * authorizationService.deleteAuthorization('previousId')
        1 * authorizationService.saveAuthorization(authorization)
    }

    def 'throws exception if process key is missing'() {
        given:
        def processDefinitionEntity = new ProcessDefinitionStatisticsEntity()
        processDefinitionEntity.key = null

        when:
        underTest.parseProcess(processDefinitionEntity)

        then:
        thrown(InternalWorkflowException)
    }
}
