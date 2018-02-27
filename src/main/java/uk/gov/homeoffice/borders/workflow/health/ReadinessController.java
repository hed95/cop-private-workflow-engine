package uk.gov.homeoffice.borders.workflow.health;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ReadinessController {

    private ProcessEngineConfiguration processEngineConfiguration;

    @GetMapping(path = "/engine", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,String> readiness() {
        return Collections.singletonMap("engine", processEngineConfiguration.getProcessEngineName());
    }

}
