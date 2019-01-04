package uk.gov.homeoffice.borders.workflow.task.comment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import javax.validation.Valid;
import java.util.List;

/**
 * REST API for creating and getting comments associated with a given task
 * Comments cannot be updated or removed. They are immutable.
 */

@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CommentsApiController {

    private CommentsApplicationService commentsApplicationService;

    @GetMapping(path = "/api/workflow/tasks/{taskId}/comments")
    public List<TaskComment> comments(@PathVariable String taskId, PlatformUser platformUser) {
        return commentsApplicationService.comments(platformUser, taskId);
    }

    @PostMapping(path = "/api/workflow/tasks/comments", produces = "application/json", consumes = "application/json")
    public TaskComment create(@RequestBody @Valid TaskComment commentDto, PlatformUser platformUser) {
        return commentsApplicationService.create(platformUser, commentDto);
    }
}
