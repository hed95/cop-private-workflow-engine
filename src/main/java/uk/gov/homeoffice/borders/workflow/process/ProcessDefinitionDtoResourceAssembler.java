package uk.gov.homeoffice.borders.workflow.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessDefinitionDtoResourceAssembler implements RepresentationModelAssembler<ProcessDefinition, ProcessDefinitionDtoResource> {

    @Override
    public ProcessDefinitionDtoResource toModel(ProcessDefinition entity) {
        ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(entity);
        ProcessDefinitionDtoResource resource = new ProcessDefinitionDtoResource();
        resource.setProcessDefinitionDto(processDefinitionDto);
        return resource;
    }

}
