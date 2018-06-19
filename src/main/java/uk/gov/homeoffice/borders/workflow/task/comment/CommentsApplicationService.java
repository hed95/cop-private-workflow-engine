package uk.gov.homeoffice.borders.workflow.task.comment;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.task.TaskChecker;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CommentsApplicationService {

    private TaskService taskService;
    private TaskChecker taskChecker;
    private PlatformDataUrlBuilder platformDataUrlBuilder;
    private RestTemplate restTemplate;

    public List<TaskComment> comments(ShiftUser user, String taskId) {
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
        return comments.stream().sorted(Comparator.comparing(TaskComment::getCreatedOn)).collect(Collectors.toList());
    }

    public TaskComment create(ShiftUser user, TaskComment taskComment) {
        Task task = taskService.createTaskQuery().taskId(taskComment.getTaskId()).singleResult();
        applyTaskCheck(user, task);

        if (!taskComment.getStaffId().equalsIgnoreCase(user.getId())) {
           throw new IllegalArgumentException("User submitting comment not same as user currently logged in");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
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
