package uk.gov.homeoffice.borders.workflow;


import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.service.notify.NotificationClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"keycloak.enabled=false", "spring.datasource.name=testdbB"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"GOV_NOTIFY_CLIENT_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_EMAIL_TEMPLATE_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_SMS_TEMPLATE_ID = XXXX",
        "PLATFORM_DATA_ENDPOINT_URL = http://localhost:8000",
        "PLATFORM_DATA_TOKEN = DB",
        "ENGINE_DB_URL=jdbc:h2:mem:testdbB;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "ENGINE_DB_USERNAME=sa", "ENGINE_DB_PASSWORD=",
        "ENGINE_DRIVER=org.h2.Driver", "CAMUNDA_DB_TYPE=h2"})
public abstract class BaseIntClass {


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(8000));


    @Autowired
    protected TestRestTemplate testRestTemplate;

    @MockBean
    protected AccessToken accessToken;

    @MockBean
    protected KeycloakSecurityContext keycloakSecurityContext;


    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected IdentityService identityService;

    @MockBean
    protected NotificationClient notificationClient;

    @Autowired
    protected RuntimeService runtimeService;


}