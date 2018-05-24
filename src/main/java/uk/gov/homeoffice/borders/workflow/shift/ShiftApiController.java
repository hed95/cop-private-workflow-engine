package uk.gov.homeoffice.borders.workflow.shift;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This REST API is responsible for creating an active shift within the workflow platform.
 * This drives what tasks/processes/cases a user can see.
 * <p>
 * The workflow create a record in the Shift platform data service
 * and then goes to sleep. The end time of the shift then triggers the workflow to
 * remove the shift record
 */


@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(path = "/api/workflow/shift",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class ShiftApiController {

    private ShiftApplicationService shiftApplicationService;

    @PostMapping
    public ResponseEntity<Void> startShift(@RequestBody ShiftInfo shiftInfo, UriComponentsBuilder uriComponentsBuilder) {

        String email = shiftInfo.getEmail();
        log.info("Request to create shift for '{}'", email);
        ProcessInstance shiftInstance = shiftApplicationService.startShift(shiftInfo);
        log.info("Shift created '{}' for '{}'", shiftInstance.getProcessInstanceId(), email);
        UriComponents uriComponents =
                uriComponentsBuilder.path("/api/workflow/shift/{shiftIdentifier}").buildAndExpand(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ShiftInfo shiftInfo(@PathVariable String email) {
        return shiftApplicationService.getShiftInfo(email);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity deleteShift(@PathVariable String email, @RequestParam String deletedReason) {
        shiftApplicationService.deleteShift(email, deletedReason);
        return ResponseEntity.ok().build();
    }


}
