package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.*;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.engine.rest.impl.AbstractProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.process.CustomDeploymentRestService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.net.URI;

@Component
@ApplicationPath("/rest/camunda")
@Slf4j
public class JerseyConfig extends CamundaJerseyResourceConfig {

    @Autowired
    private CustomCamundaRestApiService customCamundaRestApiService;

    @Override
    protected void registerCamundaRestResources() {
        log.info("Configuring camunda rest api.");
        this.register(customCamundaRestApiService);
        this.registerClasses(CamundaRestResources.getConfigurationClasses());

        log.info("Finished configuring camunda rest api.");
    }


    @Path("")
    @Component
    public static class CustomCamundaRestApiService extends AbstractProcessEngineRestServiceImpl {

        @Value("${camunda.bpm.process-engine-name}")
        private String processEngineName;


        @Path(ExternalTaskRestService.PATH)
        public ExternalTaskRestService getExternalTaskRestService() {
            return super.getExternalTaskRestService(processEngineName);
        }

        @Path(ExecutionRestService.PATH)
        public ExecutionRestService getExecutionRestService() {
            return super.getExecutionService(processEngineName);
        }

        @Path(IncidentRestService.PATH)
        public IncidentRestService getIncidentRestService() {
            return super.getIncidentService(processEngineName);
        }

        @Path(HistoryRestService.PATH)
        public HistoryRestService getHistoryRestService() {
            return super.getHistoryRestService(processEngineName);
        }

        @Path(DeploymentRestService.PATH)
        public DeploymentRestService getDeploymentRestService() {
            String rootResourcePath = getRelativeEngineUri(processEngineName).toASCIIString();
            CustomDeploymentRestService subResource
                    = new CustomDeploymentRestService(processEngineName, getObjectMapper());
            subResource.setRelativeRootResourceUri(rootResourcePath);
            return super.getDeploymentRestService(processEngineName);
        }

        @Path(JobDefinitionRestService.PATH)
        public JobDefinitionRestService getJobDefinitionRestService() {
            return super.getJobDefinitionRestService(processEngineName);
        }

        @Path(JobRestService.PATH)
        public JobRestService getJobRestService() {
            return super.getJobRestService(processEngineName);
        }

        @Path(ProcessInstanceRestService.PATH)
        public ProcessInstanceRestService getProcessInstanceRestService() {
            return super.getProcessInstanceService(processEngineName);
        }

        @Path(ProcessDefinitionRestService.PATH)
        public ProcessDefinitionRestService getProcessDefinitionRestService() {
            return super.getProcessDefinitionService(processEngineName);
        }

        @Path(MessageRestService.PATH)
        public MessageRestService getMessageRestService() {
            return super.getMessageRestService(processEngineName);
        }


        @Path(TaskRestService.PATH)
        public TaskRestService getTaskRestService() {
            return super.getTaskRestService(processEngineName);
        }

        @Path(DecisionDefinitionRestService.PATH)
        public DecisionDefinitionRestService getDecisionDefinitionRestService() {
            return super.getDecisionDefinitionRestService(processEngineName);
        }

        @Path(AuthorizationRestService.PATH)
        public AuthorizationRestService getAuthorizationRestService() {
            return super.getAuthorizationRestService(processEngineName);
        }


        @Override
        protected URI getRelativeEngineUri(String engineName) {
            return URI.create("/");
        }
    }

}
