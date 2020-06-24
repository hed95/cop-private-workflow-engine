package uk.gov.homeoffice.borders.workflow.process

import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.springframework.hateoas.PagedModel
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
        Authorization newAuthorization = authorizationService
                .createNewAuthorization(Authorization.AUTH_TYPE_GRANT)
        newAuthorization.setGroupId("custom_role")
        newAuthorization.setResource(Resources.PROCESS_DEFINITION)
        newAuthorization.setResourceId("candidateGroupsWorkflow")
        newAuthorization.addPermission(Permissions.READ)
        authorizationService.saveAuthorization(newAuthorization)

        when:
        def result = mvc.perform(get('/api/workflow/process-definitions')
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())
        def pagedResources = objectMapper.readValue(result.andReturn().response.contentAsString, PagedModel)
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
