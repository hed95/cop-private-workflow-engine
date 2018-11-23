package uk.gov.homeoffice.borders.workflow.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.process.ProcessDefinitionAuthorizationParserPlugin;
import uk.gov.homeoffice.borders.workflow.security.KeycloakClient;
import uk.gov.homeoffice.borders.workflow.shift.ShiftUserMethodArgumentResolver;
import uk.gov.homeoffice.borders.workflow.task.TaskFilterCriteriaMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableRetry
@EnableCaching
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
    public RestTemplate restTemplate(KeycloakClient keycloakClient, RestTemplateBuilder builder) {
        KeycloakBearerTokenInterceptor keycloakBearerTokenInterceptor =
                new KeycloakBearerTokenInterceptor(keycloakClient);
        RestTemplate restTemplate = builder.build();
        restTemplate.getInterceptors().add(keycloakBearerTokenInterceptor);
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

        @Autowired
        private IdentityService identityService;

        @Override
        public void addArgumentResolvers(
                List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new ShiftUserMethodArgumentResolver(identityService));
            argumentResolvers.add(new TaskFilterCriteriaMethodArgumentResolver());

        }
    }
}
