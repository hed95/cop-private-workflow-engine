package uk.gov.homeoffice.borders.workflow.task.comment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CommentsApiController {

    private CommentsApplicationService commentsApplicationService;
    private RestApiUserExtractor restApiUserExtractor;

    @GetMapping(path = "/api/workflow/tasks/{taskId}/comments")
    public List<CommentDto> comments(@PathVariable String taskId) {

        return commentsApplicationService.comments(restApiUserExtractor.toUser(), taskId)
                .stream()
                .map(CommentDto::fromComment)
                .collect(toList());
    }
}
