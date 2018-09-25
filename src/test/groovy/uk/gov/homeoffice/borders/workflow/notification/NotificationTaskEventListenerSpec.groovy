package uk.gov.homeoffice.borders.workflow.notification

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.Spin
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.plugin.variable.SpinValues
import org.springframework.http.MediaType
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.exception.ExceptionHandler
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification
import uk.gov.homeoffice.borders.workflow.task.notifications.NotificationTaskEventListener
import uk.gov.homeoffice.borders.workflow.task.notifications.Priority
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.SendEmailResponse
import uk.gov.service.notify.SendSmsResponse

class NotificationTaskEventListenerSpec extends Specification {

    NotificationTaskEventListener underTest

    NotificationClient notificationClient = Mock(NotificationClient)


    def setup() {
        underTest = new NotificationTaskEventListener(notificationClient, "emailNotificationTemplateId", "smsNotificationTemplateId",
        new ExceptionHandler(), new JacksonJsonDataFormat("application/jackson", new ObjectMapper()), "https://", "awb://")
    }

    def 'can handle standard notification'() {
        Notification updated
        given:
        def notification = new Notification()
        notification.subject = "subject"
        notification.payload = "payload"
        notification.email = "email"
        def priority = new Priority()
        priority.setNotificationBoost(true)
        priority.type = Priority.Type.STANDARD
        notification.priority = priority
        def notificationVariable =  Variables.objectValue(notification)
                .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                .create()

        and:
        def delegateTask = Mock(DelegateTask)
        delegateTask.getVariable("taskType") >> "notification"
        delegateTask.getVariableTyped("notification", true) >> notificationVariable
        delegateTask.getProcessInstanceId() >> "processInstanceId"

        and:
        def response = Mock(SendEmailResponse)
        def emailResponseUUID = UUID.randomUUID()
        response.getNotificationId() >> emailResponseUUID

        1 * notificationClient.sendEmail('emailNotificationTemplateId', "email",
                ['payload': 'payload', 'externalLink': '', 'subject' : 'subject'], '/api/workflow/notifications/processInstanceId') >> response


        when:
        underTest.notify(delegateTask)

        then:
        1 * delegateTask.setVariable(_,_) >> {
            arguments -> updated = Spin.S(arguments[1]).mapTo(Notification)
        }

        updated.emailNotificationId == emailResponseUUID.toString()
    }


    def 'can handle standard notification as spin variable'() {
        Notification updated
        given:
        def notification = new Notification()
        notification.subject = "subject"
        notification.payload = "payload"
        notification.email = "email"
        def priority = new Priority()
        priority.setNotificationBoost(true)
        priority.type = Priority.Type.STANDARD
        notification.priority = priority
        def notificationVariable =SpinValues.jsonValue(Spin.JSON(notification)).create()

        and:
        def delegateTask = Mock(DelegateTask)
        delegateTask.getVariable("taskType") >> "notification"
        delegateTask.getVariableTyped("notification", true) >> notificationVariable
        delegateTask.getProcessInstanceId() >> "processInstanceId"

        and:
        def response = Mock(SendEmailResponse)
        def emailResponseUUID = UUID.randomUUID()
        response.getNotificationId() >> emailResponseUUID

        1 * notificationClient.sendEmail('emailNotificationTemplateId', "email",
                ['payload': 'payload', 'externalLink': '', 'subject' : 'subject'], '/api/workflow/notifications/processInstanceId') >> response


        when:
        underTest.notify(delegateTask)

        then:
        1 * delegateTask.setVariable(_,_) >> {
            arguments -> updated = Spin.S(arguments[1]).mapTo(Notification)
        }

        updated.emailNotificationId == emailResponseUUID.toString()
    }


    def 'can handle urgent notification'() {
        Notification updatedNotification
        given:
        def notification = new Notification()
        notification.subject = "subject"
        notification.payload = "payload"
        notification.email = "email"
        notification.mobile = "mobile"
        notification.externalLink = "click on this link https://private-ui.homeoffice.gov.uk"
        def priority = new Priority()
        priority.setNotificationBoost(true)
        priority.type = Priority.Type.URGENT
        notification.priority = priority
        def notificationVariable =  Variables.objectValue(notification)
                .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                .create()

        and:
        def delegateTask = Mock(DelegateTask)
        delegateTask.getVariable("taskType") >> "notification"
        delegateTask.getVariableTyped("notification", true) >> notificationVariable
        delegateTask.getProcessInstanceId() >> "processInstanceId"
        delegateTask.getId() >> "id"

        and:
        def emailResponse = Mock(SendEmailResponse)
        def smsResponse = Mock(SendSmsResponse)

        def emailResponseUUID = UUID.randomUUID()
        emailResponse.getNotificationId() >> emailResponseUUID

        def smsResponseUUID = UUID.randomUUID()
        smsResponse.getNotificationId() >> smsResponseUUID

        1 * notificationClient.sendEmail('emailNotificationTemplateId', "email",
                ['payload': 'payload', 'externalLink': 'click on this link https://private-ui.homeoffice.gov.uk', 'subject' : 'URGENT: subject'], '/api/workflow/notifications/processInstanceId') >> emailResponse

        1 * notificationClient.sendSms('smsNotificationTemplateId', "mobile",
                ['payload': 'payload', 'externalLink': 'click on this link awb://private-ui.homeoffice.gov.uk', 'subject' : 'URGENT: subject'], '/api/workflow/notifications/processInstanceId') >> smsResponse


        when:
        underTest.notify(delegateTask)

        then:
        1 * delegateTask.setVariable(_,_) >> {
            arguments -> updatedNotification = Spin.S(arguments[1]).mapTo(Notification)
        }

        updatedNotification.emailNotificationId == emailResponseUUID.toString()
        updatedNotification.smsNotificationId == smsResponseUUID.toString()

    }

    def 'can handle emergency notification'() {
        Notification updatedNotification
        given:
        def notification = new Notification()
        notification.subject = "subject"
        notification.payload = "payload"
        notification.email = "email"
        notification.mobile = "mobile"
        def priority = new Priority()
        priority.setNotificationBoost(true)
        priority.type = Priority.Type.EMERGENCY
        notification.priority = priority
        def notificationVariable =  Variables.objectValue(notification)
                .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                .create()

        and:
        def delegateTask = Mock(DelegateTask)
        delegateTask.getVariable("taskType") >> "notification"
        delegateTask.getVariableTyped("notification", true) >> notificationVariable
        delegateTask.getProcessInstanceId() >> "processInstanceId"

        and:
        def emailResponse = Mock(SendEmailResponse)
        def smsResponse = Mock(SendSmsResponse)

        def emailResponseUUID = UUID.randomUUID()
        emailResponse.getNotificationId() >> emailResponseUUID

        def smsResponseUUID = UUID.randomUUID()
        smsResponse.getNotificationId() >> smsResponseUUID

        1 * notificationClient.sendEmail('emailNotificationTemplateId', "email",
                ['payload': 'payload', 'externalLink': '', 'subject' : 'EMERGENCY: subject'], '/api/workflow/notifications/processInstanceId') >> emailResponse

        1 * notificationClient.sendSms('smsNotificationTemplateId', "mobile",
                ['payload': 'payload', 'externalLink': '', 'subject' : 'EMERGENCY: subject'], '/api/workflow/notifications/processInstanceId') >> smsResponse


        when:
        underTest.notify(delegateTask)

        then:
        1 * delegateTask.setVariable(_,_) >> {
            arguments -> updatedNotification = Spin.S(arguments[1]).mapTo(Notification)
        }

        updatedNotification.emailNotificationId == emailResponseUUID.toString()
        updatedNotification.smsNotificationId == smsResponseUUID.toString()

    }



}
