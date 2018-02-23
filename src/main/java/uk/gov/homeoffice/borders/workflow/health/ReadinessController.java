package uk.gov.homeoffice.borders.workflow.health;

import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ReadinessController {

    private ProcessEngineConfiguration processEngineConfiguration;

    @GetMapping(path = "/api/engine", produces = MediaType.APPLICATION_JSON_VALUE)
    public String readiness() {
        return processEngineConfiguration.getProcessEngineName();
    }

}
