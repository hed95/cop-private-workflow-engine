package uk.gov.homeoffice.borders.workflow;

import com.tngtech.jgiven.integration.spring.EnableJGiven;
import com.tngtech.jgiven.integration.spring.SimpleSpringScenarioTest;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"security.basic.enabled=false",
                "keycloak.enabled=false"} )
@ActiveProfiles("test")
@EnableJGiven
public abstract class BaseTestClass<T> extends SimpleSpringScenarioTest<T> {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @MockBean
    protected AccessToken accessToken;

    @MockBean
    protected KeycloakSecurityContext keycloakSecurityContext;
}
