package uk.gov.homeoffice.borders.workflow.process;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ProcessApplicationServiceTest extends BaseIntClass {

    @Autowired
    private ProcessApplicationService processApplicationService;

    @Test
    public void canGetLatestProcessDefinitions() {

        //when
        Page<ProcessDefinition> processDefinitions = processApplicationService.processDefinitions(null, new PageRequest(0, 20));

        //then
        assertThat(processDefinitions, notNullValue());

    }
}
