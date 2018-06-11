package uk.gov.homeoffice.borders.workflow.notification

import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification
import uk.gov.homeoffice.borders.workflow.task.notifications.Priority

import static com.github.tomakehurst.wiremock.http.Response.response
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title('Notification API Spec')
class NotificationApiControllerSpec extends BaseSpec {


    def 'can create notification at /api/workflow/notifications'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()

        restApiUserExtractor.toUser() >> user

        and:
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
                                "phone" : "phone"
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        wireMockStub.stub {
            request {
                method 'GET'
                url '/staffview?staffid=in.(staffid)'
            }
            response {
                status: 200
                body """
                        [{
                          "phone": "phone",
                          "email": "email",
                          "gradetypeid": "gradetypeid",
                          "firstname": "firstname",
                          "surname": "surname",
                          "qualificationtypes": [
                            {
                              "qualificationname": "dummy",
                              "qualificationtype": "1"
                            },
                            {
                              "qualificationname": "staff",
                              "qualificationtype": "2"
                            }
                          ],
                          "staffid": "staffid",
                          "gradename": "grade"
                        }]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }


        when:
        def asString = objectMapper.writeValueAsString(notification)
        def result = mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())
        and:
        1 * notificationClient.sendEmail('XXXX', 'email',
                ['subject': 'URGENT: Alert',
                 'payload': 'Some payload'], _ as String)
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


    ShiftUser createUser() {
        def user = new ShiftUser()
        user.email = 'email'
        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team

        user

    }

    def 'can get notification for user at /api/workflow/notifications'() {
        given:
        def notification = createNotification()

        and:
        def user = createUser()
        restApiUserExtractor.toUser() >> user

        and:
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
                                "phone" : "phone"
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        wireMockStub.stub {
            request {
                method 'GET'
                url '/staffview?staffid=in.(staffid)'
            }
            response {
                status: 200
                body """
                        [{
                          "phone": "phone",
                          "email": "email",
                          "gradetypeid": "gradetypeid",
                          "firstname": "firstname",
                          "surname": "surname",
                          "qualificationtypes": [
                            {
                              "qualificationname": "dummy",
                              "qualificationtype": "1"
                            },
                            {
                              "qualificationname": "staff",
                              "qualificationtype": "2"
                            }
                          ],
                          "staffid": "staffid",
                          "gradename": "grade"
                        }]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }


        and:
        def asString = objectMapper.writeValueAsString(notification)
        mvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON))


        when:
        restApiUserExtractor.toUser() >> user
        def result = mvc.perform(get("/api/workflow/notifications")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())

    }
}
