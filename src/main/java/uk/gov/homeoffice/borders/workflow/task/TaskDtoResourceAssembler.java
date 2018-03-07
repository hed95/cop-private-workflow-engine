package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.hateoas.Link;
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
//        resource.add(createProcessLink(taskDto));
//        resource.add(createCommentsLink(taskDto));
//        resource.add(createFormsLink(taskDto));
        return resource;
    }

    private Link createFormsLink(TaskDto entity) {
        return null;
    }

    private Link createCommentsLink(TaskDto entity) {
        return null;
    }

    private Link createProcessLink(TaskDto entity) {
        return null;
    }
}
