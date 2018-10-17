package uk.gov.homeoffice.borders.workflow.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;

@Service
public class KeycloakClient {
    private final URI authUrl;
    private final String clientId;
    private final String clientSecret;
    private RestTemplate restTemplate;

    public KeycloakClient(@Value("${keycloak.auth-server-url}") final String authUrl,
                          @Value("${keycloak.realm}") final String authRealm,
                          @Value("${keycloak.resource}") final String clientId,
                          @Value("${keycloak.credentials.secret}") String clientSecret) throws URISyntaxException {
        this.authUrl = new URI(authUrl + "/realms/" + authRealm + "/protocol/openid-connect/token");
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = new RestTemplate();
    }

    public String bearerToken() {
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", authHeader());

        final HttpEntity<?> entity = new HttpEntity<>(body, headers);

        final ResponseEntity<KeycloakResult> result = restTemplate.postForEntity(authUrl, entity, KeycloakResult.class);
        return result.getBody().getAccessToken();
    }


    private String authHeader() {
        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        return "Basic " + new String(encodedAuth);
    }

    @Data
    public static class KeycloakResult {
        @JsonProperty("access_token")
        private String accessToken;
    }

}
