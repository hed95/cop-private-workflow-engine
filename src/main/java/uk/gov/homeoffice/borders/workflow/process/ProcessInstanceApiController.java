package uk.gov.homeoffice.borders.workflow.process;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import javax.validation.Valid;
import java.util.Map;

import static uk.gov.homeoffice.borders.workflow.process.ProcessApiPaths.PROCESS_INSTANCE_ROOT_API;

/**
 * REST API for creating a process instance and deleting
 * More endpoints will be exposed over time.
 */

@RestController
@RequestMapping(path = PROCESS_INSTANCE_ROOT_API)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessInstanceApiController {

    private ProcessApplicationService processApplicationService;
    private ObjectMapper objectMapper;

    @DeleteMapping("/{processInstanceId}")
    @ApiOperation("Deletes a process instance.")
    public ResponseEntity delete(@PathVariable String processInstanceId,
                                 @ApiParam("The reason for deletion.") @RequestParam String reason) {
        processApplicationService.delete(processInstanceId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE )
    @ApiOperation("Start a new process.")
    public ProcessInstanceDto createInstance(@RequestBody @Valid ProcessStartDto processStartDto, PlatformUser platformUser)
            throws JsonProcessingException {
        log.debug("Process data received '{}'", objectMapper.writeValueAsString(processStartDto));
        ProcessInstance processInstance = processApplicationService.createInstance(processStartDto, platformUser);
        return ProcessInstanceDto.fromProcessInstance(processInstance);

    }

    @GetMapping(value = "/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a process instance.")
    public ProcessInstanceDto processInstance(@PathVariable String processInstanceId, PlatformUser platformUser) {
        ProcessInstance processInstance = processApplicationService.getProcessInstance(processInstanceId, platformUser);
        return ProcessInstanceDto.fromProcessInstance(processInstance);
    }
    @GetMapping(value = "/{processInstanceId}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the process variables for a process instance.")
    public Map<String, VariableValueDto> variables(@PathVariable String processInstanceId,
                                                   PlatformUser platformUser) {
        VariableMap variables = processApplicationService.variables(processInstanceId, platformUser);
        return VariableValueDto.fromVariableMap(variables);
    }
}
