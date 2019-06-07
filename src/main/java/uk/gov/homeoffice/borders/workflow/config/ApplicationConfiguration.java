package uk.gov.homeoffice.borders.workflow.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.rest.*;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.process.ProcessDefinitionAuthorizationParserPlugin;
import uk.gov.homeoffice.borders.workflow.security.KeycloakClient;
import uk.gov.homeoffice.borders.workflow.shift.ShiftUserMethodArgumentResolver;
import uk.gov.homeoffice.borders.workflow.task.TaskFilterCriteriaMethodArgumentResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableRetry
@EnableAsync
@EnableConfigurationProperties(PlatformDataBean.class)
public class ApplicationConfiguration {

    @Autowired
    private PlatformDataBean platformDataBean;

    @Bean
    public JacksonJsonDataFormat formatter(ObjectMapper objectMapper) {
        return new JacksonJsonDataFormat("application/json", objectMapper);
    }

    @Bean
    public ProcessEnginePlugin spinProcessEnginePlugin() {
        return new SpinProcessEnginePlugin();
    }


    @Bean
    public PlatformDataUrlBuilder platformDataQueryBuilder() {
        return new PlatformDataUrlBuilder(platformDataBean);
    }

    @Bean
    @Order(Ordering.DEFAULT_ORDER - 1)
    public ProcessDefinitionAuthorizationParserPlugin processDefinitionAuthorizationParserPlugin() {
        return new ProcessDefinitionAuthorizationParserPlugin();
    }

    @Bean
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setTaskDecorator(new MDCContextTaskDecorator());
        return executor;
    }

    @Bean
    public RestTemplate restTemplate(KeycloakClient keycloakClient,
                                     RestTemplateBuilder builder,
                                     CorrelationIdInterceptor correlationIdInterceptor) {
        KeycloakBearerTokenInterceptor keycloakBearerTokenInterceptor =
                new KeycloakBearerTokenInterceptor(keycloakClient);
        builder.setConnectTimeout(platformDataBean.getConnectTimeout());
        builder.setReadTimeout(platformDataBean.getReadTimeout());
        RestTemplate restTemplate = builder.build();
        restTemplate.getInterceptors().add(keycloakBearerTokenInterceptor);
        restTemplate.getInterceptors().add(correlationIdInterceptor);
        return restTemplate;
    }

    public  static class MDCContextTaskDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    MDC.setContextMap(contextMap);
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }
    }

    @Configuration
    public static class MVCMethodConfig implements WebMvcConfigurer {
        private static String[] corsPaths = {
               "/api",
                ExternalTaskRestService.PATH,
                ExecutionRestService.PATH,
                IncidentRestService.PATH,
                HistoryRestService.PATH,
                DeploymentRestService.PATH,
                JobDefinitionRestService.PATH,
                JobRestService.PATH,
                ProcessInstanceRestService.PATH,
                ProcessDefinitionRestService.PATH,
                MessageRestService.PATH,
                TaskRestService.PATH,
                AuthorizationRestService.PATH
        };

        @Autowired
        private IdentityService identityService;
        @Autowired
        private PrivateUiBean privateUiBean;

        @Override
        public void addArgumentResolvers(
                List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new ShiftUserMethodArgumentResolver(identityService));
            argumentResolvers.add(new TaskFilterCriteriaMethodArgumentResolver());

        }

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            Arrays.stream(corsPaths).forEach(p -> registry.addMapping(p + "/**")
                                                          .allowedOrigins(privateUiBean.getUrl().toString(), "http://localhost:8080")
                                                          .allowedMethods("HEAD", "GET", "POST", "DELETE"));
        }
    }
}
