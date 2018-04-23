package uk.gov.homeoffice.borders.workflow.session;

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
 * This REST API is responsible for creating an active session workflow.
 * The workflow create a record in the ActiveSession in the reference data service
 * and then goes to sleep. The end time of the active session then triggers the workflow to
 * remove the record in the active session and remove the keycloak session
 *
 */


@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(path = "/api/workflow/sessions", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class SessionApiController {

    private SessionApplicationService sessionApplicationService;

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody ActiveSession activeSession, UriComponentsBuilder uriComponentsBuilder) {
        log.info("Active session request created for session id '{}'", activeSession.getSessionId());
        ProcessInstance session = sessionApplicationService.createSession(activeSession);
        log.info("Session created '{}'", session.getProcessInstanceId());
        UriComponents uriComponents =
                uriComponentsBuilder.path("/api/workflow/sessions/{sessionIdentifier}").buildAndExpand(activeSession.getSessionId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/{sessionIdentifier}")
    public ActiveSession sessionInfo(@PathVariable String sessionIdentifier) {
        return sessionApplicationService.getActiveSession(sessionIdentifier);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId, @RequestParam String deletedReason) {
        sessionApplicationService.deleteSession(sessionId, deletedReason);
        return ResponseEntity.ok().build();
    }


}
