package uk.gov.homeoffice.borders.workflow.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentWithDefinitionsDto;
import org.camunda.bpm.engine.rest.impl.DeploymentRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;

import javax.ws.rs.core.UriInfo;


@Slf4j
public class CustomDeploymentRestService extends DeploymentRestServiceImpl {

    private final ProcessDefinitionAuthorizationParser processDefinitionAuthorizationParser;
    public CustomDeploymentRestService(String engineName, ObjectMapper objectMapper) {
        super(engineName, objectMapper);
        processDefinitionAuthorizationParser = new ProcessDefinitionAuthorizationParser(
                super.getProcessEngine().getAuthorizationService()
        );
    }

    @Override
    public DeploymentWithDefinitionsDto createDeployment(UriInfo uriInfo, MultipartFormData payload) {
        DeploymentWithDefinitionsDto deployment = super.createDeployment(uriInfo, payload);

        try {
            deployment.getDeployedProcessDefinitions().entrySet().stream()
                    .findFirst().ifPresent(entry -> {
                        ProcessDefinitionEntity processDefinitionEntity =
                                (ProcessDefinitionEntity) getProcessEngine()
                                        .getRepositoryService().createProcessDefinitionQuery()
                                        .processDefinitionId(entry.getKey()).singleResult();
                        processDefinitionAuthorizationParser.parseProcess(
                                processDefinitionEntity
                        );

                    }

            );
        } catch (Exception e) {
            log.error("Failed to create authorization for process definition '{}'", e.getMessage());
        }

        return deployment;
    }
}
