package uk.gov.homeoffice.borders.workflow;


import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.camunda.bpm.engine.IdentityService;
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
        properties = {"security.basic.enabled=false",
                "keycloak.enabled=false"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {"GOV_NOTIFY_CLIENT_ID = XXXX", "" +
        "GOV_NOTIFY_NOTIFICATION_EMAIL_TEMPLATE_ID = XXXX", "" +
        "GOV_NOTIFY_NOTIFICATION_SMS_TEMPLATE_ID = XXXX",
        "PREST_ENDPOINT_URL = http://localhost:8000",
        "TX_DB_NAME = DB"})
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



}