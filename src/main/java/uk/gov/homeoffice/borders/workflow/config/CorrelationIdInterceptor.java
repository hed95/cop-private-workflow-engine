package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    public static final String CORRELATION_HEADER_NAME = "nginxId";
    private final HttpServletRequest request;


    CorrelationIdInterceptor(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (RequestContextHolder.getRequestAttributes() != null) {
            final String correlationId = this.request.getHeader("nginxId");
            if (correlationId != null) {
                request.getHeaders().set(CORRELATION_HEADER_NAME, correlationId);
            }
        }
        return execution.execute(request, body);
    }
}
