package uk.gov.homeoffice.borders.workflow.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engine")
public class HealthController {

    @GetMapping(path = "/healthz")
    public ResponseEntity<?> healthz() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/readiness")
    public ResponseEntity readiness() {
        return ResponseEntity.ok().build();
    }
}
