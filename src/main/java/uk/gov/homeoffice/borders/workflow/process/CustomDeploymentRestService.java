package uk.gov.homeoffice.borders.workflow.process;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResource;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Custom extension to allow authorizations to be created when uploading
 * BPMNs via Camunda REST API. Using composition over inheritance.
 */
@Slf4j
public class CustomDeploymentRestService implements DeploymentRestService {

    private DeploymentRestService deploymentRestService;
    private ProcessDefinitionAuthorizationParser processDefinitionAuthorizationParser;
    private ProcessEngine processEngine;

    public CustomDeploymentRestService(DeploymentRestService deploymentRestService, ProcessEngine processEngine) {
        this.deploymentRestService = deploymentRestService;
        if (deploymentRestService == null) {
            throw new InternalWorkflowException("Deployment service cannot be null");
        }
        this.processEngine = processEngine;
        if (processEngine == null) {
            throw new InternalWorkflowException("Process engine cannot be null");
        }
        this.processDefinitionAuthorizationParser = new ProcessDefinitionAuthorizationParser(
                processEngine.getAuthorizationService()
        );
    }

    @Override
    public DeploymentResource getDeployment(String deploymentId) {
        return deploymentRestService.getDeployment(deploymentId);
    }

    @Override
    public List<DeploymentDto> getDeployments(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
        return deploymentRestService.getDeployments(uriInfo, firstResult, maxResults);
    }

    @Override
    public DeploymentDto createDeployment(UriInfo uriInfo, MultipartFormData multipartFormData) {
        DeploymentDto deployment = deploymentRestService.createDeployment(uriInfo, multipartFormData);

        try {
            ProcessDefinitionEntity processDefinitionEntity =
                    (ProcessDefinitionEntity) processEngine
                            .getRepositoryService().createProcessDefinitionQuery()
                            .deploymentId(deployment.getId()).singleResult();

            if (processDefinitionEntity != null) {
                ProcessDefinitionEntity processDefinition =
                        (ProcessDefinitionEntity)
                                processEngine.getRepositoryService()
                                        .getProcessDefinition(processDefinitionEntity.getId());
                if (processDefinition != null) {
                    processDefinitionAuthorizationParser.parseProcess(
                            processDefinition
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to create authorization for process definition '{}'", e.getMessage());
        }
        return deployment;
    }

    @Override
    public CountResultDto getDeploymentsCount(UriInfo uriInfo) {
        return deploymentRestService.getDeploymentsCount(uriInfo);
    }
}
