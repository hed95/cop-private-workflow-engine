package uk.gov.homeoffice.borders.workflow.process


import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.springframework.http.MediaType
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static org.hamcrest.Matchers.is
import static org.hamcrest.core.IsNull.notNullValue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProcessInstanceApiControllerSpec extends BaseSpec {


    def 'can create process instance'() {
        given:
        def processStartDto = createProcessStartDto()


        and:
        logInUser()


        when:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())

    }

    def 'can get process instance'() {
        given:
        def processStartDto = createProcessStartDto()

        and:
        logInUser()

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def response = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceResponse)

        when:
        def processInstance = objectMapper.readValue(mvc.perform(get("/api/workflow/process-instances/${response.processInstance.id}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn().response.contentAsString, ProcessInstanceDto)


        then:
        processInstance
        processInstance.id == response.processInstance.id

    }


    def 'can delete process instance'() {
        given:
        def processStartDto = createProcessStartDto()

        and:
        logInUser()

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def dto = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceResponse)

        def processInstanceId = dto.processInstance.id
        when:
        mvc.perform(delete("/api/workflow/process-instances/${processInstanceId}?reason=finised")
                .contentType(MediaType.APPLICATION_JSON))


        then:
        def response = mvc.perform(get("/api/workflow/process-instances/${processInstanceId}")
                .contentType(MediaType.APPLICATION_JSON))

        response.andExpect(status().isNotFound())
    }

    def "can get process variables"() {
        given:
        def processStartDto = createProcessStartDto()

        and:
        logInUser()

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def dto = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceResponse)

        when:
        def response = mvc.perform(get("/api/workflow/process-instances/${dto.processInstance.id}/variables")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        response.andExpect(status().is2xxSuccessful())
        response.andExpect(jsonPath('$.collectionOfData', is(notNullValue())))

    }

    def "exception thrown if process start dto missing process key"() {
        given:
        def processStartDto = createProcessDtoWithMissingAttributes(false, true)

        and:
        logInUser()

        when:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is4xxClientError())

    }

    def "exception thrown if process start dto missing variable name"() {
        given:
        def processStartDto = createProcessDtoWithMissingAttributes(true, false)

        and:
        logInUser()

        when:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is4xxClientError())

    }

    ProcessStartDto createProcessStartDto() {
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'test'
        processStartDto.variableName = 'collectionOfData'
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto
    }


    ProcessStartDto createProcessDtoWithMissingAttributes(boolean includeProcessKey, boolean includeVariableName) {
        def processStartDto = new ProcessStartDto()
        if (includeProcessKey) {
            processStartDto.processKey = 'test'

        }

        if (includeVariableName) {
            processStartDto.variableName = 'collectionOfData'
        }
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto
    }


}
