package uk.gov.homeoffice.borders.workflow.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class CorrelationIdInterceptorTest extends Specification {
    def request = Mock(HttpServletRequest)
    def clientRequest = Mock(HttpRequest)
    def execution = Mock(ClientHttpRequestExecution)
    def headers = Mock(HttpHeaders)
    def interceptor = new CorrelationIdInterceptor(request)

    def shouldAddHeaderIfPresent() {
        when:
            interceptor.intercept(clientRequest, null, execution)

        then:
            1 * request.getHeader("nginxId") >> "correlationId"
            1 * clientRequest.getHeaders() >> headers
            1 * headers.set("nginxId", "correlationId")
            1 * execution.execute(clientRequest, null)
    }

    def shouldNotAddHeaderIfNotPresent() {
        when:
            interceptor.intercept(clientRequest, null, execution)

        then:
            1 * request.getHeader("nginxId") >> null
            0 * clientRequest.getHeaders()
            1 * execution.execute(clientRequest, null)

    }
}
