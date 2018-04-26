package uk.gov.homeoffice.borders.workflow.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResource;

@Component
@Slf4j
public class ProcessDefinitionDtoResourceAssembler implements ResourceAssembler<ProcessDefinition, ProcessDefinitionDtoResource> {

    @Override
    public ProcessDefinitionDtoResource toResource(ProcessDefinition entity) {
        ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(entity);
        ProcessDefinitionDtoResource resource = new ProcessDefinitionDtoResource();
        resource.setProcessDefinitionDto(processDefinitionDto);
        return resource;
    }

}
