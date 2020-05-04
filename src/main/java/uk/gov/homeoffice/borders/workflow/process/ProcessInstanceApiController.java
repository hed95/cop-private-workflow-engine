package uk.gov.homeoffice.borders.workflow.process;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.task.TaskDtoResource;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
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
    public ProcessInstanceResponse createInstance(@RequestBody @Valid ProcessStartDto processStartDto,
                                                  PlatformUser platformUser)
            throws JsonProcessingException {
        log.debug("Process data received '{}'", objectMapper.writeValueAsString(processStartDto));
        Tuple2<ProcessInstance, List<Task>> response = processApplicationService.createInstance(processStartDto,
                platformUser);

        List<TaskDto> tasks = response._2().stream().map(TaskDto::fromEntity).collect(toList());
        return new ProcessInstanceResponse((ProcessInstanceWithVariablesDto) ProcessInstanceWithVariablesDto
                .fromProcessInstance((ProcessInstanceWithVariables)response._1()), tasks);

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
        return VariableValueDto.fromMap(variables);
    }
}
