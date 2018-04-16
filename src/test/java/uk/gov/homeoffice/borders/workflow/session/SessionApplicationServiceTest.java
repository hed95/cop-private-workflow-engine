package uk.gov.homeoffice.borders.workflow.session;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.hamcrest.Matchers;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class SessionApplicationServiceTest extends BaseIntClass {

    @Autowired
    private SessionApplicationService sessionApplicationService;

    @Autowired
    private ExternalTaskService externalTaskService;


    @Test
    public void canCreateAnActiveSession() throws Exception {
        //given
        ActiveSession activeSession = new ActiveSession();
        activeSession.setEmail("testEmail");
        activeSession.setSessionId("sessionId");
        activeSession.setEndTime(LocalDateTime.now().plusSeconds(10).toDate());
        activeSession.setPersonId(null);
        activeSession.setSessionId(UUID.randomUUID().toString());
        activeSession.setSessionType("workflow");

        //when
        ProcessInstance session = sessionApplicationService.createSession(activeSession);
        List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();

        //then
        Assert.assertThat(session, is(notNullValue()));
        Assert.assertThat(externalTasks.size(), is(Matchers.not(0)));
    }
}
