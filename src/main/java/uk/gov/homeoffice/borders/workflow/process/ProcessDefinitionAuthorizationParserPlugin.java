package uk.gov.homeoffice.borders.workflow.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventParseListener;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProcessDefinitionAuthorizationParserPlugin extends AbstractCamundaConfiguration {


    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
        if (preParseListeners == null) {
            preParseListeners = new ArrayList<>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        preParseListeners.add(new ProcessInstanceAuthorizationParser(processEngineConfiguration.getAuthorizationService()));
    }

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


    @Slf4j
    @AllArgsConstructor
    public static class ProcessInstanceAuthorizationParser extends ProcessApplicationEventParseListener {

        private AuthorizationService authorizationService;

        @Override
        public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
            processDefinition.addListener(ExecutionListener.EVENTNAME_START,
                    new ProcessInstanceAuthorizationListener(authorizationService));

        }
    }

}
