package uk.gov.homeoffice.borders.workflow.resource;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(Ordering.DEFAULT_ORDER + 1)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BpmnResourceConfiguration extends AbstractCamundaConfiguration {

    public EngineResourceLoader engineResourceLoader;

    @Override
    public void postInit(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        log.info("Loading process definitions from persistent store '{}'...", engineResourceLoader.storeType());

        List<ResourceContainer> resources = engineResourceLoader.getResources();

        log.info("Number of resources to load into engine '{}'", resources.size());
        RepositoryService repositoryService = springProcessEngineConfiguration.getRepositoryService();

        resources.stream()
                .forEach(resource -> {
                     try {
                         log.info("Loading '{}' to engine", resource.getName());
                         repositoryService
                                 .createDeployment()
                                 .addInputStream(resource.getName(), resource.getResource().getInputStream());
                         log.info("'{}' loaded into engine", resource.getName());
                     } catch (Exception e) {
                         log.error("An exception occurred while trying to load '{}' to the engine", e);
                     }
                });

        log.info("Process definitions loaded from persistent store '{}'", engineResourceLoader.storeType());
    }
}
