package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import uk.gov.homeoffice.borders.workflow.security.KeycloakClient;

import java.io.IOException;

import static java.lang.String.format;

@Slf4j
public class KeycloakBearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private KeycloakClient keycloakClient;

    KeycloakBearerTokenInterceptor(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("Authorization", format("Bearer %s", keycloakClient.bearerToken()));
        return execution.execute(request, body);
    }
}
