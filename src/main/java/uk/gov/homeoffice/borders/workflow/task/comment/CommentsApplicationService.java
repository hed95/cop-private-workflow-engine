package uk.gov.homeoffice.borders.workflow.task.comment;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.task.TaskChecker;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CommentsApplicationService {

    private TaskService taskService;
    private TaskChecker taskChecker;

    public List<Comment> comments(User user, String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        applyTaskCheck(user, task);
        return taskService.getTaskComments(task.getId());
    }

    public Comment create(User user, CommentDto commentDto) {
        Task task = taskService.createTaskQuery().taskId(commentDto.getTaskId()).singleResult();
        applyTaskCheck(user, task);
        return taskService.createComment(commentDto.getTaskId(), task.getProcessInstanceId(), commentDto.getMessage());
    }

    private void applyTaskCheck(User user, Task task) {
        if (task == null) {
            throw new ResourceNotFound("Task does not exist");
        }
        taskChecker.checkUserAuthorized(user, task);
    }

}
