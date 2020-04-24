package uk.gov.homeoffice.borders.workflow.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class CorrelationIdInterceptorSpec extends Specification {
    def request = Mock(HttpServletRequest)
    def clientRequest = Mock(HttpRequest)
    def execution = Mock(ClientHttpRequestExecution)
    def headers = Mock(HttpHeaders)
    def requestContext = Mock(RequestAttributes);
    def interceptor = new CorrelationIdInterceptor(request)

    def shouldAddHeaderIfPresent() {
        when:
            RequestContextHolder.setRequestAttributes(requestContext);
            interceptor.intercept(clientRequest, null, execution)

        then:
            1 * request.getHeader("nginxId") >> "correlationId"
            1 * clientRequest.getHeaders() >> headers
            1 * headers.set("nginxId", "correlationId")
            1 * execution.execute(clientRequest, null)
    }

    def shouldNotAddHeaderIfNotPresent() {
        when:
            RequestContextHolder.setRequestAttributes(requestContext);
            interceptor.intercept(clientRequest, null, execution)

        then:
            1 * request.getHeader("nginxId") >> null
            0 * clientRequest.getHeaders()
            1 * execution.execute(clientRequest, null)

    }

    def shouldNotAddHeaderIfNotInRequest() {
        when:
        interceptor.intercept(clientRequest, null, execution)

        then:
        0 * request.getHeader("nginxId") >> null
        0 * clientRequest.getHeaders()
        1 * execution.execute(clientRequest, null)
    }


    void cleanup() {
        RequestContextHolder.setRequestAttributes(null);
    }
}
