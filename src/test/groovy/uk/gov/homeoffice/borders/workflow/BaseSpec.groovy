package uk.gov.homeoffice.borders.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication
import uk.gov.service.notify.NotificationClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["keycloak.enabled=false", "spring.datasource.name=testdbB"])
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = ["GOV_NOTIFY_CLIENT_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_EMAIL_TEMPLATE_ID = XXXX",
        "GOV_NOTIFY_NOTIFICATION_SMS_TEMPLATE_ID = XXXX",
        "PLATFORM_DATA_ENDPOINT_URL = http://localhost:8000",
        "PLATFORM_DATA_TOKEN = DB",
        "ENGINE_DB_URL=jdbc:h2:mem:testdbB;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "ENGINE_DB_USERNAME=sa", "ENGINE_DB_PASSWORD=",
        "ENGINE_DB_DRIVER=org.h2.Driver", "CAMUNDA_DB_TYPE=h2",
        "PUBLIC_UI_PROTOCOL=https://",
        "PUBLIC_UI_TEXT_PROTOCOL=awb://"])
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

    def setup() {
        def instances = runtimeService.createProcessInstanceQuery().list() as ProcessInstance[]
        def ids = instances.collect {
            it -> it.processInstanceId
        }
        runtimeService.deleteProcessInstances(ids, "testclean", true, true)
    }

    ShiftUser logInUser() {
        def user = new ShiftUser()
        user.id = 'test'
        user.email = 'test'

        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user
    }

    @lombok.Data
    class Data {
        String assignee
        String candidateGroup
        String name
        String description

    }

    @Configuration
    static class StubConfig {
        def detachedMockFactory = new DetachedMockFactory()

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
        RestTemplate keycloakRestTemplate() {
            return new RestTemplate()
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeRequests().anyRequest().permitAll()
        }
    }
}