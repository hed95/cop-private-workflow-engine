package uk.gov.homeoffice.borders.workflow.health

import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.MediaType
import spock.lang.Title
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Health API Spec")
class HealthApiControllerSpec extends BaseSpec{

    def setup() {
        stubFor(WireMock.get('/_cluster/health/')
                .willReturn(aResponse()
                        .withHeader('Content-Type', 'application/json')
                        .withBody("""
                                        {
                                            
                                        }
                                        """)))
    }

    def 'can get health check' () {
        when:
        def result = mvc.perform(get('/actuator/health')
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())

    }

    def 'can get readiness check' (){
        when:
        def result = mvc.perform(get('/engine')
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
    }
}
