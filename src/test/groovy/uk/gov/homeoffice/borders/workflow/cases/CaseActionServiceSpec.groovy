package uk.gov.homeoffice.borders.workflow.cases

import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultEntriesImpl
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl
import org.camunda.bpm.engine.DecisionService
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser

class CaseActionServiceSpec extends Specification {

    RepositoryService repositoryService = Mock()
    FormService formService = Mock()
    DecisionService decisionService = Mock()
    CaseActionService caseActionService

    def setup() {
        caseActionService = new CaseActionService(
                repositoryService,
                formService,
                decisionService
        )
    }

    def 'can get actions'() {
        given: 'actions returned'
        DmnDecisionResultEntriesImpl entry = Mock()
        entry.get('actionProcessKey') >> 'processKey'
        def decisionResult = new DmnDecisionResultImpl([entry])

        DecisionsEvaluationBuilder builder = Mock()
        decisionService.evaluateDecisionByKey(_) >> builder
        builder.variables(_) >> builder
        builder.evaluate() >> decisionResult


        and: 'process definition query returns default'
        ProcessDefinitionQuery first = Mock()
        ProcessDefinition definition = new ProcessDefinitionEntity()
        definition.setKey("generate-case-pdf")
        repositoryService.createProcessDefinitionQuery() >> first
        first.latestVersion() >> first
        first.processDefinitionKey("generate-case-pdf") >> first
        first.singleResult() >> definition


        and: 'process definition for dmn result'
        ProcessDefinitionQuery last = Mock()
        ProcessDefinition lastDefinition = new ProcessDefinitionEntity()
        lastDefinition.setKey("processKey")
        lastDefinition.setId("id")
        lastDefinition.setName("processName")
        repositoryService.createProcessDefinitionQuery() >> last
        last.latestVersion() >> first
        last.processDefinitionKeysIn("processKey") >> last
        last.list() >> [lastDefinition]

        and: 'form key exists'
        formService.getStartFormKey(_) >> 'formKey'

        when: 'evaluate actions'
        def caseDetails = new CaseDetail()
        def instance = new CaseDetail.ProcessInstanceReference()
        instance.setKey("test")
        caseDetails.processInstances = [
            instance
        ]
        def platformUser = new PlatformUser()
        def result = caseActionService.getAvailableActions(
                caseDetails, platformUser
        )

        then: 'result should be 2'
        result.size() == 2

    }

    def 'returns default action if decision returns no result'() {

    }

    def 'returns default action if decision fails'() {

    }
}
