package uk.gov.homeoffice.borders.workflow.process

import org.springframework.hateoas.PagedResources
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Process Definition API spec")
class ProcessDefinitionApiControllerSpec extends BaseSpec {

    def 'can get process definitions from /api/workflow/process-definitions'() {
        given:
        logInUser()

        when:
        def result = mvc.perform(get('/api/workflow/process-definitions')
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())
        def pagedResources = objectMapper.readValue(result.andReturn().response.contentAsString, PagedResources)
        pagedResources.metadata.totalElements != 0
    }

    def 'can get process definition /api/workflow/process-definitions/test'() {
        given:
        logInUser()

        when:
        def result = mvc.perform(get('/api/workflow/process-definitions/test')
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        def processDefinitionDtoResource = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessDefinitionDtoResource)
        processDefinitionDtoResource.formKey == 'test'
        processDefinitionDtoResource.processDefinitionDto.key == 'test'
    }

}
