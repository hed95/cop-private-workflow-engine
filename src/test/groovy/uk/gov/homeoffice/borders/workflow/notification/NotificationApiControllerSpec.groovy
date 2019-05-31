package uk.gov.homeoffice.borders.workflow.notification


import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication
import uk.gov.homeoffice.borders.workflow.task.TaskReference
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification
import uk.gov.homeoffice.borders.workflow.task.notifications.Priority

import static com.github.tomakehurst.wiremock.http.Response.response
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title('Notification API Spec')
class NotificationApiControllerSpec extends BaseSpec {


    def 'can create notification at /api/workflow/notifications'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)


        and:
        stubGetShift()


        when:
        def asString = objectMapper.writeValueAsString(notification)
        def result = mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())
        and:
        1 * notificationClient.sendEmail('XXXX', 'email',
                ['subject': 'URGENT: Alert',
                 'payload': 'Some payload', 'externalLink': ''], _ as String)
    }

    def 'can get notification for user at /api/workflow/notifications'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)


        and:
        stubGetShift()


        and:
        def asString = objectMapper.writeValueAsString(notification)
        mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))


        when:
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)

        def result = mvc.perform(get("/api/workflow/notifications")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())

    }

    def 'can cancel notifications'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)


        and:
        stubGetShift()

        and:
        def asString = objectMapper.writeValueAsString(notification)
        def result = mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))
        def processInstance = objectMapper.readValue(result.andReturn().response.getContentAsString(), ProcessInstanceDto)

        when:
        def response = mvc.perform(delete("/api/workflow/notifications/${processInstance.id}?reason=test").content(asString)
                .contentType(MediaType.APPLICATION_JSON))

        then:
        response.andReturn().response.status == OK.value()
    }


    def 'can acknowledge a notification'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)


        and:
        stubGetShift()

        and:
        def asString = objectMapper.writeValueAsString(notification)
        def result = mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))
        def processInstance = objectMapper.readValue(result.andReturn().response.getContentAsString(), ProcessInstanceDto)
        def task = taskService.createTaskQuery().processInstanceId(processInstance.id).list().first()

        when:
        def response = mvc.perform(delete("/api/workflow/notifications/task/${task.id}")
                .contentType(MediaType.APPLICATION_JSON))
        def taskReference = objectMapper.readValue(response.andReturn().response.getContentAsString(), TaskReference)

        then:
        taskReference
        taskReference.id == task.id
        taskReference.status == TaskListener.EVENTNAME_COMPLETE

    }

    private stubGetShift() {
        wireMockStub.stub {
            request {
                method "GET"
                url "/shift?teamid=eq.teamA"
            }
            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamid",
                                "phone" : "phone",
                                "email" : "email"
                                
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }
    }


    Notification createNotification() {
        def notification = new Notification()
        notification.teamId = 'teamA'
        notification.subject = 'Alert'
        notification.payload = 'Some payload'
        def priority = new Priority()
        priority.notificationBoost = false
        priority.type = Priority.Type.URGENT
        notification.priority = priority

        notification
    }


    PlatformUser createUser() {
        def user = new PlatformUser()
        user.email = 'email'
        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team

        user

    }
}
