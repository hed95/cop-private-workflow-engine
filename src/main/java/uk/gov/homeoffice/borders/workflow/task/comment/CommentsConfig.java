package uk.gov.homeoffice.borders.workflow.task.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Bean
    public CommentsApplicationService commentsApplicationService(TaskService taskService,
                                                                 TaskChecker taskChecker,
                                                                 PlatformDataUrlBuilder platformDataUrlBuilder,
                                                                 RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new CommentsApplicationService(taskService, taskChecker, platformDataUrlBuilder,
                restTemplate, objectMapper);
    }
}
