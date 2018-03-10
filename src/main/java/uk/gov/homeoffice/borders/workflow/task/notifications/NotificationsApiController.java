package uk.gov.homeoffice.borders.workflow.task.notifications;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResource;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResourceAssembler;

import static uk.gov.homeoffice.borders.workflow.task.notifications.NotificationsApiPaths.ROOT_PATH;

@RestController
@RequestMapping(path = ROOT_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationsApiController {

    private RestApiUserExtractor restApiUserExtractor;
    private TaskDtoResourceAssembler taskDtoResourceAssembler;
    private PagedResourcesAssembler<Task> pagedResourcesAssembler;
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> notifications(@RequestBody Notification notification) {
        ProcessInstance processInstance = notificationService.create(notification);
        if (processInstance != null) {
            log.info("Notification workflow created");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public PagedResources<TaskDtoResource> notifications(Pageable pageable) {
        Page<Task> page = notificationService.notifications(restApiUserExtractor.toUser(), pageable);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);
    }
}
