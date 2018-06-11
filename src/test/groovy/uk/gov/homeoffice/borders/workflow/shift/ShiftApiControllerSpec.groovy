package uk.gov.homeoffice.borders.workflow.shift

import org.camunda.bpm.engine.runtime.ProcessInstance
import org.joda.time.LocalDateTime
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static com.github.tomakehurst.wiremock.http.Response.response
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Shift API Spec")
class ShiftApiControllerSpec extends BaseSpec {



    def 'can create a shift at /api/workflow/shift'() {
        given:
        def shift = createActiveShift()

        when:
        def result = mvc.perform(post('/api/workflow/shift')
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(shift)))

        then:
        result.andExpect(status().isCreated())

    }

    def "can get shift info at /api/workflow/shift"() {
        given:
        def shift = createActiveShift()
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


    ShiftInfo createActiveShift() {
        ShiftInfo shiftInfo = new ShiftInfo()
        shiftInfo.setEmail("testEmail")
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate())
        shiftInfo.setStaffId(UUID.randomUUID().toString())
        shiftInfo.setShiftHours(1)
        shiftInfo.setShiftMinutes(0)
        shiftInfo
    }
}
