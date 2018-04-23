package uk.gov.homeoffice.borders.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.ExternalTaskRestService;
import org.camunda.bpm.engine.rest.impl.AbstractProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.net.URI;

@Component
@ApplicationPath("/rest/camunda")
@Slf4j
public class JerseyConfig extends CamundaJerseyResourceConfig {

    @Autowired
    private CustomCamundaRestApiService customCamundaRestApiService;

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

        @Override
        protected URI getRelativeEngineUri(String engineName) {
            return URI.create("/");
        }
    }

}