package uk.gov.homeoffice.borders.workflow.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ProcessDefinitionAuthorizationParserPlugin extends AbstractCamundaConfiguration {

    @Override
    public void postProcessEngineBuild(ProcessEngine engine) {
        List<ProcessDefinition> processDefinitions = engine.getRepositoryService()
                .createProcessDefinitionQuery()
                .latestVersion()
                .active()
                .list();
        ProcessDefinitionAuthorizationParser processDefinitionAuthorizationParser =
                new ProcessDefinitionAuthorizationParser(engine.getAuthorizationService());

        processDefinitions.forEach(p -> {
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) engine.getRepositoryService().getProcessDefinition(p.getId());
            processDefinitionAuthorizationParser.parseProcess(processDefinition);
        });

    }
}
