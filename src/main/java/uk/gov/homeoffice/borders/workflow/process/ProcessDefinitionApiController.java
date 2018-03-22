package uk.gov.homeoffice.borders.workflow.process;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.homeoffice.borders.workflow.process.ProcessApiPaths.PROCESS_DEFINITION_ROOT_API;

@RestController
@RequestMapping(path = PROCESS_DEFINITION_ROOT_API)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessDefinitionApiController {

    private ProcessApplicationService processApplicationService;
    private RestApiUserExtractor restApiUserExtractor;

    @GetMapping
    public List<ProcessDefinitionDto> processDefinitions() {
        List<ProcessDefinition> definitions = processApplicationService.processDefinitions(restApiUserExtractor.toUser());
        return definitions.stream().map(ProcessDefinitionDto::fromProcessDefinition).collect(Collectors.toList());
    }

    @GetMapping("/{processDefinition}/form")
    public String formKey(@PathVariable String processDefinition) {
        return processApplicationService.formKey(processDefinition);
    }
}
