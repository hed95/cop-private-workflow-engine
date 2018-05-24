package uk.gov.homeoffice.borders.workflow.process;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.RestApiUserExtractor;

import java.util.Map;

import static uk.gov.homeoffice.borders.workflow.process.ProcessApiPaths.PROCESS_INSTANCE_ROOT_API;

/**
 * REST API for creating a process instance and deleting
 * More endpoints will be exposed over time.
 */

@RestController
@RequestMapping(path = PROCESS_INSTANCE_ROOT_API,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE )
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessInstanceApiController {

    private ProcessApplicationService processApplicationService;
    private RestApiUserExtractor restApiUserExtractor;


    @DeleteMapping
    public ResponseEntity delete(@PathVariable String processInstanceId, @RequestParam String reason) {
        processApplicationService.delete(processInstanceId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<ProcessInstanceDto> createInstance(@RequestBody ProcessStartDto processStartDto) {

        ProcessInstance processInstance = processApplicationService.createInstance(processStartDto, restApiUserExtractor.toUser());
        ProcessInstanceDto processInstanceDto = ProcessInstanceDto.fromProcessInstance(processInstance);

        return ResponseEntity.ok(processInstanceDto);

    }

    @GetMapping("/{processInstanceId}")
    private ProcessInstanceDto processInstance(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = processApplicationService.getProcessInstance(processInstanceId, restApiUserExtractor.toUser());
        return ProcessInstanceDto.fromProcessInstance(processInstance);
    }
    @GetMapping("/{processInstanceId}/variables")
    private Map<String, VariableValueDto> variables(@PathVariable String processInstanceId) {
        VariableMap variables = processApplicationService.variables(processInstanceId, restApiUserExtractor.toUser());
        return VariableValueDto.fromVariableMap(variables);
    }
}
