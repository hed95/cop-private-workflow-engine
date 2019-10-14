package uk.gov.homeoffice.borders.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptionPlugin
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptor
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.Rule
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ClassPathResource
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import uk.gov.homeoffice.borders.workflow.config.CorrelationIdInterceptor
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication
import uk.gov.service.notify.NotificationClient

import static com.github.tomakehurst.wiremock.client.WireMock.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["keycloak.enabled=false", "spring.datasource.name=testdbB", "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"])
@ActiveProfiles("test,local")
@AutoConfigureMockMvc
@TestPropertySource(properties = ["GOV_NOTIFY_API_KEY = XXXX",
        "GOV_NOTIFY_NOTIFICATION_EMAIL_TEMPLATE_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_SMS_TEMPLATE_ID = XXXX",
        "API_COP_URI = http://localhost:8000",
        "API_REF_URI = http://localhost:8000",
        "API_REF_PROTOCOL = http://",
        "API_REF_PORT = 8080",
        "WWW_UI_PROTOCOL=http://",
        "PLATFORM_DATA_TOKEN = DB",
        "DB_ENGINE_URI=jdbc:h2:mem:testdbB;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "DB_ENGINE_DEFAULT_USERNAME=sa",
        "DB_ENGINE_DEFAULT_PASSWORD=",
        "DB_ENGINE_DRIVER=org.h2.Driver",
        "DB_ENGINE_DEFAULT_DBNAME=postgres",
        "DB_ENGINE_TYPE=h2",
        "WWW_UI_TXT_PROTOCOL=awb://",
        "ENGINE_NAME=cop",
        "ENGINE_CORS=http://localhost:8000",
        "KEYCLOAK_URI=http://localhost:9000/auth",
        "KEYCLOAK_REALM=myRealm",
        "ENGINE_PRIVATE_DER_PATH=/keys/private_key.der",
        "ENGINE_PUBLIC_DER_PATH=/keys/public_key.der",
        "ENGINE_KEYCLOAK_CLIENT_SECRET=very_secret",
        "ENGINE_KEYCLOAK_CLIENT_ID=client_id"])
abstract class BaseSpec extends Specification {

    @Autowired
    public MockMvc mvc

    @Autowired
    public ObjectMapper objectMapper

    @Autowired
    public RuntimeService runtimeService

    @Autowired
    public TaskService taskService

    @Autowired
    public NotificationClient notificationClient

    @Autowired
    public IdentityService identityService


    def wmPort = 8000

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    public wireMockStub = new WireMockGroovy(wmPort)

    def stubKeycloak() {
        stubFor(post("/realms/myRealm/protocol/openid-connect/token")
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                .withHeader("Authorization", equalTo("Basic Y2xpZW50X2lkOnZlcnlfc2VjcmV0"))
                .withRequestBody(equalTo("grant_type=client_credentials"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                                        {
                                            "access_token": "MY_SECRET_TOKEN"
                                        }
                                        """)))
    }

    def setup() {
        def instances = runtimeService.createProcessInstanceQuery().list() as ProcessInstance[]
        def ids = instances.collect {
            it -> it.processInstanceId
        }
        runtimeService.deleteProcessInstances(ids, "testclean", true, true)
        stubKeycloak()
    }

    PlatformUser logInUser() {
        def user = new PlatformUser()
        user.id = 'test'
        user.email = 'test'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles =  ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user
    }

    @lombok.Data
    class Data {
        String assignee
        String candidateGroup
        String name
        String description
        String candidateUser

    }

    @Configuration
    static class StubConfig {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor() {
            return new ProcessInstanceSpinVariableDecryptor(
                    new ClassPathResource("/keys/private_key.der")
                        .getFile().getAbsolutePath()
            )
        }

        @Bean
        ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor() {
            return new ProcessInstanceSpinVariableEncryptor(
                    new ClassPathResource("/keys/public_key.der")
                            .getFile().getAbsolutePath()
            )
        }


        @Bean
        ProcessInstanceSpinVariableEncryptionPlugin plugin() {
            return new ProcessInstanceSpinVariableEncryptionPlugin(processInstanceSpinVariableEncryptor(),
                    processInstanceSpinVariableDecryptor())
        }


        @Bean
        MockPostProcessor mockPostProcessor() {
            new MockPostProcessor(
                    [
                            'identityService'   : detachedMockFactory.Stub(IdentityService),
                            'notificationClient': detachedMockFactory.Mock(NotificationClient)
                    ]
            )
        }
    }

    static class MockPostProcessor implements BeanFactoryPostProcessor {
        private final Map<String, Object> mocks

        MockPostProcessor(Map<String, Object> mocks) {
            this.mocks = mocks
        }

        void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            mocks.each { name, mock ->
                beanFactory.registerSingleton(name, mock)
            }
        }
    }

    @Configuration
    static class TestConfig extends WebSecurityConfigurerAdapter {


        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @Primary
        RestTemplate keycloakRestTemplate(RestTemplateBuilder builder, CorrelationIdInterceptor interceptor) {
            final RestTemplate restTemplate = builder.build()
            restTemplate.getInterceptors().add(interceptor)
            return restTemplate
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().cors().and().authorizeRequests().anyRequest().permitAll()
        }
    }
}
