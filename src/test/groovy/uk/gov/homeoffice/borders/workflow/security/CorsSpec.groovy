package uk.gov.homeoffice.borders.workflow.security

import org.springframework.http.HttpHeaders
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CorsSpec extends BaseSpec {

    def "OPTIONS request should include cors header"() {
        given:
            logInUser()

        when:
            def result = mvc.perform(options("/api/workflow/tasks")
                            .header("Origin", "http://localhost:8000")
                            .header("Access-Control-Request-Method", "GET"))

        then:
            result.andExpect(status().is2xxSuccessful())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))

    }

    def "GET request for Camunda API should include cors header"() {
        given:
            logInUser()

        when:
            def result = mvc.perform(get("/rest/camunda/process-definition/key/foo")
                .header("Origin", "http://localhost:8000")
                .header("Access-Control-Request-Method", "GET"))

        then:
            result.andExpect(status().is4xxClientError())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))

    }

    def "OPTIONS request for Camunda API should include cors header"() {
        given:
            logInUser()

        when:
            def result = mvc.perform(options("/rest/camunda/process-definition/key/foo")
                .header("Origin", "http://localhost:8000")
                .header("Access-Control-Request-Method", "GET"))

        then:
            result.andExpect(status().is2xxSuccessful())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))

    }

    def "GET request should include cors header"() {
        given:
            logInUser()

        when:
            def result = mvc.perform(get("/api/workflow/tasks")
                            .header("Origin", "http://localhost:8000"))

        then:
            result.andExpect(status().is2xxSuccessful())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))

    }
}
