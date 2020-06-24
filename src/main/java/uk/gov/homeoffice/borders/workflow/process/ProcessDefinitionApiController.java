package uk.gov.homeoffice.borders.workflow.process;


import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import static uk.gov.homeoffice.borders.workflow.process.ProcessApiPaths.PROCESS_DEFINITION_ROOT_API;

/**
 * REST API for getting a list of process definitions and also endpoint for getting the form key associated with
 * a process definition
 */

@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessDefinitionApiController {

    private ProcessApplicationService processApplicationService;
    private ProcessDefinitionDtoResourceAssembler processDefinitionDtoResourceAssembler;
    private PagedResourcesAssembler<ProcessDefinition> pagedResourcesAssembler;

    @GetMapping(value = PROCESS_DEFINITION_ROOT_API, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the list of process definitions available to the current user.")
    public PagedModel<ProcessDefinitionDtoResource> processDefinitions(Pageable pageable, PlatformUser platformUser) {
        Page<ProcessDefinition> page = processApplicationService.processDefinitions(platformUser, pageable);
        return pagedResourcesAssembler.toModel(page, processDefinitionDtoResourceAssembler);
    }


    @GetMapping(PROCESS_DEFINITION_ROOT_API + "/{processKey}")
    @ApiOperation("Get a process definition.")
    public ProcessDefinitionDtoResource processDefinition(@PathVariable String processKey) {
        ProcessDefinition definition = processApplicationService.getDefinition(processKey);
        String formKey = processApplicationService.formKey(definition.getId());
        ProcessDefinitionDtoResource processDefinitionDtoResource = new ProcessDefinitionDtoResource();
        processDefinitionDtoResource.setProcessDefinitionDto(ProcessDefinitionDto.fromProcessDefinition(definition));
        processDefinitionDtoResource.setFormKey(formKey);
        return processDefinitionDtoResource;
    }



}
