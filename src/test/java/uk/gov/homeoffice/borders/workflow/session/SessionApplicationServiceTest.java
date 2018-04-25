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
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

public class SessionApplicationServiceTest extends BaseIntClass {

    @Autowired
    private SessionApplicationService sessionApplicationService;

    @Autowired
    private ExternalTaskService externalTaskService;


    @Test
    public void canCreateAnActiveSession() {
        //given
        ActiveSession activeSession = createActiveSession();

        //when
        ProcessInstance session = sessionApplicationService.createSession(activeSession);
        List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();

        //then
        Assert.assertThat(session, is(notNullValue()));
        Assert.assertThat(externalTasks.size(), is(Matchers.not(0)));
    }

    @Test
    public void canCreateActiveSessionWithPerson() {
        //given
        ActiveSession activeSession = createActiveSession();
        activeSession.setPersonId("person");

        //when
        ProcessInstance session = sessionApplicationService.createSession(activeSession);
        List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();

        //then
        Assert.assertThat(session, is(notNullValue()));
        Assert.assertThat(externalTasks.size(), is(Matchers.not(0)));
    }

    @Test
    public void canGetActiveSession() {
        //given
        ActiveSession activeSession = createActiveSession();

        //and
        sessionApplicationService.createSession(activeSession);

        //when
        ActiveSession loaded = sessionApplicationService.getActiveSession(activeSession.getSessionId());

        //then
        assertThat(loaded, is(notNullValue()));
    }


    @Test(expected = ResourceNotFound.class)
    public void canDeleteActiveSession() {

        //given
        ActiveSession activeSession = createActiveSession();

        wireMockRule.stubFor(delete(urlEqualTo("/DB/public/activesession?sessionid=" + activeSession.getSessionId()))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withStatus(200)));

        //and
        sessionApplicationService.createSession(activeSession);

        //when
        sessionApplicationService.deleteSession(activeSession.getSessionId(), "no need");

        //then
        sessionApplicationService.getActiveSession(activeSession.getSessionId());

    }

    private ActiveSession createActiveSession() {
        ActiveSession activeSession = new ActiveSession();
        activeSession.setEmail("testEmail");
        activeSession.setSessionId("sessionId");
        activeSession.setStartTime(LocalDateTime.now().toDate());
        activeSession.setPersonId(null);
        activeSession.setSessionId(UUID.randomUUID().toString());
        activeSession.setSessionType("workflow");
        activeSession.setShiftHours(1);
        activeSession.setShiftMinutes(0);
        return activeSession;
    }
}
