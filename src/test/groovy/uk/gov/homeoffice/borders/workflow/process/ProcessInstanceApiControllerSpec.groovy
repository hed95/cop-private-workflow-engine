package uk.gov.homeoffice.borders.workflow.process

import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.springframework.http.MediaType
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static org.hamcrest.Matchers.is
import static org.hamcrest.core.IsNull.notNullValue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class ProcessInstanceApiControllerSpec extends BaseSpec {


    def 'can create process instance'() {
        given:
        def processStartDto = createProcessStartDto()


        and:
        def user = logInUser()
        restApiUserExtractor.toUser() >> user

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
        def user = logInUser()
        restApiUserExtractor.toUser() >> user

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def processInstanceDto = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceDto)

        when:
        def processInstance = objectMapper.readValue(mvc.perform(get("/api/workflow/process-instances/${processInstanceDto.id}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn().response.contentAsString, ProcessInstanceDto)


        then:
        processInstance
        processInstance.id == processInstanceDto.id

    }


    def 'can delete process instance'() {
        given:
        def processStartDto = createProcessStartDto()

        and:
        def user = logInUser()
        restApiUserExtractor.toUser() >> user

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def processInstanceDto = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceDto)

        when:
        mvc.perform(delete("/api/workflow/process-instances/${processInstanceDto.id}?reason=finised")
                .contentType(MediaType.APPLICATION_JSON))


        then:
        def response = mvc.perform(get("/api/workflow/process-instances/${processInstanceDto.id}")
                .contentType(MediaType.APPLICATION_JSON))

        response.andExpect(status().isNotFound())
    }

    def "can get process variables"() {
        given:
        def processStartDto = createProcessStartDto()

        and:
        def user = logInUser()
        restApiUserExtractor.toUser() >> user

        and:
        def result = mvc.perform(post("/api/workflow/process-instances")
                .content(objectMapper.writeValueAsString(processStartDto))
                .contentType(MediaType.APPLICATION_JSON))

        def processInstanceDto = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessInstanceDto)

        when:
        def response = mvc.perform(get("/api/workflow/process-instances/${processInstanceDto.id}/variables")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        response.andExpect(status().is2xxSuccessful())
        response.andExpect(jsonPath('$.collectionOfData', is(notNullValue())))

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




}
