package uk.gov.homeoffice.borders.workflow.shift;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser.ShiftDetails;

import javax.validation.Valid;

import static java.util.Optional.ofNullable;

/**
 * This REST API is responsible for creating an active shift within the workflow platform.
 * This drives what tasks/processes/cases a user can see.
 * <p>
 * The workflow creates a record in the Shift platform data service
 * and then goes to sleep. The end time of the shift then triggers the workflow to
 * remove the shift record
 */


@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(path = "/api/workflow/shift")
public class ShiftApiController {

    private ShiftApplicationService shiftApplicationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Start a new shift for the current user.")
    public ResponseEntity<?> startShift(@RequestBody @Valid ShiftDetails shiftInfo, UriComponentsBuilder uriComponentsBuilder) {

        String email = shiftInfo.getEmail();
        log.info("Request to create shift for '{}'", email);
        ProcessInstance shiftInstance = shiftApplicationService.startShift(shiftInfo);
        log.info("Shift created '{}' for '{}'", shiftInstance.getProcessInstanceId(), email);
        UriComponents uriComponents =
                uriComponentsBuilder.path("/api/workflow/shift/{shiftIdentifier}").buildAndExpand(email);

        return ResponseEntity.created(uriComponents.toUri())
                .body((ProcessInstanceWithVariablesDto) ProcessInstanceWithVariablesDto
                        .fromProcessInstance((ProcessInstanceWithVariables) shiftInstance));

    }

    @GetMapping(path = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the user's shift details.")
    public ShiftDetails shiftInfo(@PathVariable String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email required");
        }
        return shiftApplicationService.getShiftInfo(email);
    }


    @DeleteMapping("/{email}")
    @ApiOperation("Delete the shift for the specified user.")
    public ResponseEntity deleteShift(@PathVariable String email, @RequestParam @ApiParam(required = true, value = "The reason for deletion.") String deletedReason) {
        shiftApplicationService.deleteShift(email, deletedReason);
        return ResponseEntity.ok().build();
    }


}
