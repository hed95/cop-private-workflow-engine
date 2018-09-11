package uk.gov.homeoffice.borders.workflow.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;

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
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean
    public PlatformDataUrlBuilder platformDataQueryBuilder() {
        return new PlatformDataUrlBuilder(platformDataBean);
    }

    @Bean
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setTaskDecorator(new MDCContextTaskDecorator());
        return executor;
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

}
