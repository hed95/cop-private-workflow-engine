package uk.gov.homeoffice.borders.workflow.shift


import org.joda.time.LocalDateTime
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication

import static com.github.tomakehurst.wiremock.http.Response.response
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Shift API Spec")
class ShiftApiControllerSpec extends BaseSpec {



    def 'can create a shift at /api/workflow/shift'() {
        given:
        def shift = createActiveShift()

        and:
        deleteShift()

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shift)))

        then:
        result.andExpect(status().isCreated())

    }

    private deleteShift() {
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?email=eq.testEmail'

            }
            response {
                status 200
                body """
                        [{
                          "shiftid": "shiftId",
                          "enddatetime": "2018-11-06T16:23:26"
                        }]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }
        wireMockStub.stub {
            request {
                method 'DELETE'
                url '/shift?shiftid=eq.shiftId'

            }
            response {
                status 204
                headers {
                    "Content-Type" "application/json"
                }
            }
        }
    }

    def "can get shift info at /api/workflow/shift"() {
        given:
        def shift = createActiveShift()

        and:
        deleteShift()
        logInUser()

        and:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/rf_location?locationid=eq.current'

            }
            response {
                status 200
                headers {
                    "Content-Type" "application/json"
                }
                body '''
                        {
                         "locationname" : "current"
                        }
                    '''
            }
        }

        and:
        mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shift)))

        when:
        def result = mvc.perform(get('/api/workflow/shift/testEmail')
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        ShiftInfo shiftInfo = objectMapper.readValue(result.andReturn().response.contentAsString, ShiftInfo)
        shiftInfo
        shiftInfo.getCurrentLocationName() == 'current'

    }

    def 'can delete a shift at /api/workflow/shift'() {
        given:
        def shift = createActiveShift()

        and:
        wireMockStub.stub {
            request {
                method 'DELETE'
                url '/shift?shiftid=eq.xxxxx'
            }
            response {
                status 200
            }
        }
        and:
        deleteShift()

        and:
        mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shift)))


        and:
        def instance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(shift.email).singleResult()
        runtimeService.setVariable(instance.processInstanceId, "shiftId", "xxxxx")

        when:
        def result = mvc.perform(delete('/api/workflow/shift/testEmail?deletedReason=notNeeded'))

        then:
        result.andExpect(status().is2xxSuccessful())
        def instances = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey('testEmail')
                .list()
        instances.isEmpty()

    }

    def '4xx client error thrown if shift has no start time'() {
        given:
        def shiftInfo = new ShiftInfo()
        shiftInfo.setEmail("testEmail")
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setCommandId("commandid")
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setTeamId("teamid")
        shiftInfo.setLocationId("locationid")
        shiftInfo.setCurrentLocationId("current")
        shiftInfo.setPhone("phone")
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shiftInfo)))

        then:
        result.andExpect(status().is4xxClientError())
    }

    def '4xx client error thrown if shift has command id'() {
        given:
        def shiftInfo = new ShiftInfo()
        shiftInfo.setEmail("testEmail")
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setTeamId("teamid")
        shiftInfo.setLocationId("locationid")
        shiftInfo.setCurrentLocationId("current")
        shiftInfo.setPhone("phone")
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shiftInfo)))

        then:
        result.andExpect(status().is4xxClientError())
    }

    def '4xx client error thrown if shift has no team id'() {
        def shiftInfo = new ShiftInfo()
        shiftInfo.setEmail("testEmail")
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setCommandId("commandId")
        shiftInfo.setLocationId("locationid")
        shiftInfo.setCurrentLocationId("current")
        shiftInfo.setPhone("phone")
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shiftInfo)))

        then:
        result.andExpect(status().is4xxClientError())
    }
    def '4xx client error thrown if shift has no email'() {
        def shiftInfo = new ShiftInfo()
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setCommandId("commandid")
        shiftInfo.setTeamId("teamid")
        shiftInfo.setLocationId("locationid")
        shiftInfo.setCurrentLocationId("current")
        shiftInfo.setPhone("phone")
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shiftInfo)))

        then:
        result.andExpect(status().is4xxClientError())
    }

    def '4xx client error thrown if shift has no phone'() {
        def shiftInfo = new ShiftInfo()
        shiftInfo.setEmail("email")
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setCommandId("commandid")
        shiftInfo.setTeamId("teamid")
        shiftInfo.setLocationId("locationid")
        shiftInfo.setCurrentLocationId("current")
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shiftInfo)))

        then:
        result.andExpect(status().is4xxClientError())
    }

    ShiftUser logInUser() {
        def user = new ShiftUser()
        user.id = 'test'
        user.email = 'testEmail'

        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user
    }

    ShiftInfo createActiveShift() {
        ShiftInfo shiftInfo = new ShiftInfo()
        shiftInfo.setCommandId("commandid")
        shiftInfo.setSubCommandId("subcommandid")
        shiftInfo.setTeamId("teamId")
        shiftInfo.setLocationId("location")
        shiftInfo.setPhone("phone")
        shiftInfo.setEmail("testEmail")
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo.setCurrentLocationId("current")
        shiftInfo
    }
}
