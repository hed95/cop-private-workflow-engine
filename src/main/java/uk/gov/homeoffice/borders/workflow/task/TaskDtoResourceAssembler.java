package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskDtoResourceAssembler implements ResourceAssembler<Task, TaskDtoResource>{

    @Override
    public TaskDtoResource toResource(Task entity) {
        TaskDto taskDto = TaskDto.fromEntity(entity);
        TaskDtoResource resource = new TaskDtoResource();
        resource.setTaskDto(taskDto);
        return resource;
    }
}
