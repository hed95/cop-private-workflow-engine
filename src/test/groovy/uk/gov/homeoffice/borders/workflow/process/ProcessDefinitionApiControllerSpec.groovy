package uk.gov.homeoffice.borders.workflow.process

import org.springframework.hateoas.PagedResources
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static com.github.tomakehurst.wiremock.http.Response.response
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Process Definition API spec")
class ProcessDefinitionApiControllerSpec extends BaseSpec {

    def 'can get process definitions from /api/workflow/process-definitions'() {
        given:
        logInUser()
        wireMockStub.stub {
            request {
                method 'GET'
                url '/form?name=test&select=id,name&limit=1'
            }
            response {
                status: 200
                body """
                       {
                        "total": 1,
                        "forms": [{
                          "id": "uuid",
                          "name": "test"
                        }]
                       }
                       
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

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
        wireMockStub.stub {
            request {
                method 'GET'
                url '/form?name=test'
            }
            response {
                status: 200
                body """
                      {
                      "total": 1,
                      "forms": [
                        {
                          "id": "uuid",
                          "name": "test"
                        }
                      ]
                     }
                      """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        when:
        def result = mvc.perform(get('/api/workflow/process-definitions/test')
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        def processDefinitionDtoResource = objectMapper.readValue(result.andReturn().response.contentAsString, ProcessDefinitionDtoResource)
        processDefinitionDtoResource.formKey == 'uuid'
        processDefinitionDtoResource.processDefinitionDto.key == 'test'
    }

}
