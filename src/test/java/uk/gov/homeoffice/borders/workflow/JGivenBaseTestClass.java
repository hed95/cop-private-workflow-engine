package uk.gov.homeoffice.borders.workflow;

import com.tngtech.jgiven.integration.spring.EnableJGiven;
import com.tngtech.jgiven.integration.spring.SimpleSpringScenarioTest;
import org.camunda.bpm.engine.IdentityService;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.homeoffice.borders.workflow.identity.UserService;
import uk.gov.service.notify.NotificationClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"keycloak.enabled=false", "spring.datasource.name=testdbA"})
@ActiveProfiles("test")
@EnableJGiven
@AutoConfigureMockMvc
@TestPropertySource(properties = {"GOV_NOTIFY_CLIENT_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_EMAIL_TEMPLATE_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_SMS_TEMPLATE_ID = XXXX",
        "PLATFORM_DATA_ENDPOINT_URL = http://localhost:8000",
        "PLATFORM_DATA_TOKEN = DB",
        "ENGINE_DB_URL=jdbc:h2:mem:testdbA;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "ENGINE_DB_USERNAME=sa", "ENGINE_DB_PASSWORD=",
        "ENGINE_DB_DRIVER=org.h2.Driver", "CAMUNDA_DB_TYPE=h2"})
@DirtiesContext
public abstract class JGivenBaseTestClass<T> extends SimpleSpringScenarioTest<T> {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @MockBean
    protected AccessToken accessToken;

    @MockBean
    protected KeycloakSecurityContext keycloakSecurityContext;


    @MockBean
    protected UserService userService;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected IdentityService identityService;

    @MockBean
    protected NotificationClient notificationClient;

}
