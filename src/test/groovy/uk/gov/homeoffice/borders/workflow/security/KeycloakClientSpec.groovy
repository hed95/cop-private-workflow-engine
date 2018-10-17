package uk.gov.homeoffice.borders.workflow.security

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import spock.lang.Specification
import static com.github.tomakehurst.wiremock.client.WireMock.*



class KeycloakClientSpec extends Specification {
    def wmPort = 8182

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    KeycloakClient service = new KeycloakClient("http://localhost:8182", "myRealm", "client_id", "very_secret")

    def shouldReturnAccessToken() {
        given:
        stubFor(post("/realms/myRealm/protocol/openid-connect/token")
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                .withHeader("Authorization", equalTo("Basic Y2xpZW50X2lkOnZlcnlfc2VjcmV0"))
                .withHeader("Accept", equalTo("application/json"))
                .withRequestBody(equalTo("grant_type=client_credentials"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                                        {
                                            "access_token": "MY_SECURE_TOKEN"
                                        }
                                        """)))

        when:
        def token = service.bearerToken()

        then:
        token == "MY_SECURE_TOKEN"

    }
}
