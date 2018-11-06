package uk.gov.homeoffice.borders.workflow.task.comment;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.joda.time.DateTime;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.exception.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.task.TaskChecker;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CommentsApplicationService {

    private TaskService taskService;
    private TaskChecker taskChecker;
    private PlatformDataUrlBuilder platformDataUrlBuilder;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public List<TaskComment> comments(@NotNull ShiftUser user, String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        applyTaskCheck(user, task);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<TaskComment> comments = restTemplate.exchange(platformDataUrlBuilder.getCommentsById(task.getId()), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<TaskComment>>() {
                }).getBody();
        if (comments == null) {
            return new ArrayList<>();
        }
        return comments;
    }

    public TaskComment create(ShiftUser user, TaskComment taskComment) {
        Task task = taskService.createTaskQuery().taskId(taskComment.getTaskId()).singleResult();
        applyTaskCheck(user, task);

        if (!taskComment.getStaffId().equalsIgnoreCase(user.getId())) {
           throw new IllegalArgumentException("User submitting comment not same as user currently logged in");
        }
        taskComment.setEmail(user.getEmail());
        if (taskComment.getId() == null) {
            taskComment.setId(UUID.randomUUID().toString());
        }
        if (taskComment.getCreatedOn() == null) {
            taskComment.setCreatedOn(DateTime.now().toDate());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            log.info("Task comment being sent '{}'", objectMapper.writeValueAsString(taskComment));
        } catch (JsonProcessingException e) {
            log.warn("Failed to create json for task comment", e);
        }
        restTemplate.exchange(platformDataUrlBuilder.comments(), HttpMethod.POST,
                new HttpEntity<>(taskComment, httpHeaders), TaskComment.class, new HashMap<>());

        return taskComment;
    }

    private void applyTaskCheck(ShiftUser user, Task task) {
        if (task == null) {
            throw new ResourceNotFound("Task does not exist");
        }
        taskChecker.checkUserAuthorized(user, task);
    }

}
