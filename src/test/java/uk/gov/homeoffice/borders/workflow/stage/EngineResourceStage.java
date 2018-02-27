package uk.gov.homeoffice.borders.workflow.stage;

import com.google.common.collect.Lists;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@JGivenStage
public class EngineResourceStage extends Stage<EngineResourceStage> {

    @Autowired
    private RepositoryService repositoryService;

    private List<ProcessDefinition> processDefinitions;

    public EngineResourceStage listingAllProcessDefinitions() {
        processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        return this;
    }

    public EngineResourceStage numberOfProcessDefinitionsShouldBe(int numberOfDeployments) {
        assertThat(processDefinitions.size(), is(numberOfDeployments));
        return this;
    }


    public EngineResourceStage hasDefinitions(String...names) {
        List<String> toNames = processDefinitions.stream()
               .map(ProcessDefinition::getKey).collect(toList());

        assertTrue(toNames.containsAll(Lists.newArrayList(names)));
        return this;
    }


}
