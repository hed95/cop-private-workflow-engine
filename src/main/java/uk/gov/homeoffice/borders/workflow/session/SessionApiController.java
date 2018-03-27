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

@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(path = "/api/sessions")
public class SessionApiController {

    private SessionApplicationService sessionApplicationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createSession(@RequestBody ActiveSession activeSession, UriComponentsBuilder uriComponentsBuilder) {
        ProcessInstance session = sessionApplicationService.createSession(activeSession);
        UriComponents uriComponents =
                uriComponentsBuilder.path("/api/sessions/{processInstanceId}").buildAndExpand(session.getProcessInstanceId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId, @RequestParam String deletedReason) {
        sessionApplicationService.deleteSession(sessionId, deletedReason);
        return ResponseEntity.ok().build();
    }


}
