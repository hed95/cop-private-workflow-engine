package uk.gov.homeoffice.borders.workflow;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class EngineResourceLoaderTest extends BaseTestClass {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void canLoadResource() {

        //when
        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();

        //then
        assertThat(deployments.size(), is(1));

    }
}
