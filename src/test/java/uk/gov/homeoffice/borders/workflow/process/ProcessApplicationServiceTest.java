package uk.gov.homeoffice.borders.workflow.process;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;
import uk.gov.homeoffice.borders.workflow.identity.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ProcessApplicationServiceTest extends BaseIntClass {

    @Autowired
    private ProcessApplicationService processApplicationService;

    @Test
    public void canGetLatestProcessDefinitions() {

        //when
        User user = new User();
        user.setEmail("email");
        Page<ProcessDefinition> processDefinitions = processApplicationService.processDefinitions(user, new PageRequest(0, 20));

        //then
        assertThat(processDefinitions, notNullValue());

    }
}
