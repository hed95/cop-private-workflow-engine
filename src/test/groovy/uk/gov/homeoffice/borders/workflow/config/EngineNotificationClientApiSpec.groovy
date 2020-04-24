package uk.gov.homeoffice.borders.workflow.config

import org.camunda.bpm.engine.RuntimeService
import spock.lang.Specification
import uk.gov.service.notify.NotificationClientApi

class EngineNotificationClientApiSpec extends Specification {

    RuntimeService runtimeService = Mock()
    NotificationClientApi notificationClientApi = Mock()
    NotificationClientApi engineClientApi

    def setup() {
        engineClientApi = new EngineNotificationClientApi(
                notificationClientApi,
                runtimeService
        )
    }

    def 'can send email'() {
        when: 'a call to send email is made'
        engineClientApi.sendEmail(
                'templateId',
                'email',
                ['test': 'test'],
                'executionId'
        )

        then: 'underlying notification client is invoked'
        1 * notificationClientApi.
                sendEmail('templateId', 'email',  ['test': 'test'], 'executionId')
    }

    def 'raises incident if send email fails'() {

        when: 'a call to send email is made'
        def personalisation = ['test':'test']
        notificationClientApi.sendEmail('templateId','email',
                personalisation, 'executionId') >> {
            throw new RuntimeException("failed")
        }
        engineClientApi.sendEmail(
                'templateId',
                'email',
                personalisation,
                'executionId'
        )

        then:
        thrown(RuntimeException)
        1 * runtimeService.createIncident('NOTIFICATION_FAILURE', 'failed', '{"data":{"exception":"failed","executionId":"executionId","emailAddress":"email","templateId":"templateId"}}')
    }

    def 'can send sms'() {
        when: 'a call to send sms is made'
        engineClientApi.sendSms(
                'templateId',
                'phone',
                ['test': 'test'],
                'executionId'
        )

        then: 'underlying notification client is invoked'
        1 * notificationClientApi.
                sendSms('templateId', 'phone',  ['test': 'test'], 'executionId')
    }

    def 'raises incident if send sms fails'() {

        when: 'a call to send sms is made'
        def personalisation = ['test':'test']
        notificationClientApi.sendSms('templateId','phone',
                personalisation, 'executionId') >> {
            throw new RuntimeException("failed")
        }
        engineClientApi.sendSms(
                'templateId',
                'phone',
                personalisation,
                'executionId'
        )

        then:
        thrown(RuntimeException)
        1 * runtimeService.createIncident('NOTIFICATION_FAILURE', 'failed', '{"data":{"exception":"failed","executionId":"executionId","phoneNumber":"phone","templateId":"templateId"}}')
    }
}
