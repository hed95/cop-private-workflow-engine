package uk.gov.homeoffice.borders.workflow.task.notifications;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResource;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResourceAssembler;
import uk.gov.homeoffice.borders.workflow.task.TaskReference;

import static uk.gov.homeoffice.borders.workflow.task.notifications.NotificationsApiPaths.NOTIFICATIONS_ROOT_API;

/**
 * Notifications are tasks that are created by a specific workflow
 * Once a task is created a notification is sent to the end user
 * This can be either a SMS or EMAIL or both
 */

@RestController
@RequestMapping(path = NOTIFICATIONS_ROOT_API)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationsApiController {

    private TaskDtoResourceAssembler taskDtoResourceAssembler;
    private PagedResourcesAssembler<Task> pagedResourcesAssembler;
    private NotificationService notificationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessInstanceDto> notifications(@RequestBody Notification notification,
                                                            UriComponentsBuilder uriComponentsBuilder) {
        ProcessInstance processInstance = notificationService.create(notification);
        UriComponents uriComponents =
                uriComponentsBuilder.path(NOTIFICATIONS_ROOT_API + "/process-instance/{processInstanceId}").buildAndExpand(processInstance.getProcessInstanceId());
        return  ResponseEntity
                    .created(uriComponents.toUri())
                    .body(ProcessInstanceDto.fromProcessInstance(processInstance));

    }

    @DeleteMapping("/{processInstanceId}")
    public ResponseEntity cancel(@PathVariable String processInstanceId, @RequestParam String reason) {
        notificationService.cancel(processInstanceId, reason);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<TaskReference> acknowledge(@PathVariable String taskId, PlatformUser platformUser) {
        String id = notificationService.acknowledge(platformUser, taskId);
        TaskReference taskReference = new TaskReference();
        taskReference.setId(id);
        taskReference.setStatus(TaskListener.EVENTNAME_COMPLETE);
        return ResponseEntity.ok(taskReference);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResources<TaskDtoResource> notifications(Pageable pageable,
                                                         @RequestParam(required = false, defaultValue = "false") boolean countOnly,
                                                         PlatformUser platformUser) {
        Page<Task> page = notificationService.getNotifications(platformUser, pageable, countOnly);
        return pagedResourcesAssembler.toResource(page, taskDtoResourceAssembler);
    }

}
