package uk.gov.homeoffice.borders.workflow.task

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.junit.Rule
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder
import uk.gov.homeoffice.borders.workflow.config.RefDataBean

class UserTaskEventListenerSpec extends Specification {


    def wmPort = 8900

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    def wireMockStub = new WireMockGroovy(wmPort)

    def messagingTemplate = Mock(SimpMessagingTemplate)
    def userTaskEventListener

    def setup() {
        TransactionSynchronizationManager.initSynchronization()
        def refDataBean = new RefDataBean()
        refDataBean.url="http://localhost:8900"
        def refDataUrlBuilder = new RefDataUrlBuilder(refDataBean)
        userTaskEventListener = new UserTaskEventListener(messagingTemplate, refDataUrlBuilder, new RestTemplate())
    }

    def 'can notify on team task'() {
        given:
        def delegateTask = Mock(DelegateTask)
        delegateTask.getAssignee() >> null
        delegateTask.getId() >> 'taskId'
        def identityLink = IdentityLinkEntity.newIdentityLink()
        identityLink.groupId ='teamA'
        delegateTask.getCandidates() >> [identityLink]
        delegateTask.getEventName() >> "CREATED"

        def taskReference = new TaskReference()
        taskReference.id = "taskId"
        taskReference.status = "CREATED"

        and:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/team?teamcode=in.(teamA)'
            }

            response {
                status 200
                body """ [
                            {
                                "teamid" : "id",
                                "teamcode" : "teamA",
                                "teamname" : "teamA"
                            }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }

        }

        when:
        userTaskEventListener.notify(delegateTask)

        then:
        TransactionSynchronizationManager.getSynchronizations().size() == 1
        def synchronization = TransactionSynchronizationManager.getSynchronizations().first()
        synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED)

    }
}
