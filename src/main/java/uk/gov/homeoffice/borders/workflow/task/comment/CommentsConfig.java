package uk.gov.homeoffice.borders.workflow.task.comment;

import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.config.PlatformDataBean;
import uk.gov.homeoffice.borders.workflow.task.TaskChecker;

@Configuration
public class CommentsConfig {

    @Autowired
    private PlatformDataBean platformDataBean;

    @Bean
    public CommentsApplicationService commentsApplicationService(TaskService taskService,
                                                                 TaskChecker taskChecker,
                                                                 PlatformDataUrlBuilder platformDataUrlBuilder,
                                                                 MappingJackson2HttpMessageConverter converter) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf( m -> m instanceof MappingJackson2HttpMessageConverter);
        restTemplate.getMessageConverters().add(converter);
        return new CommentsApplicationService(taskService, taskChecker, platformDataUrlBuilder,
                restTemplate, platformDataBean);
    }
}
