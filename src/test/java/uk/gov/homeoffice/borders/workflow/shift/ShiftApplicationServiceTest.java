package uk.gov.homeoffice.borders.workflow.shift;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.hamcrest.Matchers;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class ShiftApplicationServiceTest extends BaseIntClass {

    @Autowired
    private ShiftApplicationService shiftApplicationService;

    @Autowired
    private ExternalTaskService externalTaskService;

    @Before
    public void init() {

        List<String> instances = runtimeService.createProcessInstanceQuery().list()
                .stream().map(ProcessInstance::getProcessInstanceId).collect(Collectors.toList());

        runtimeService.deleteProcessInstances(instances, "test", true, true);
    }


    @Test
    public void canCreateAnActiveShift() {
        //given
        ShiftInfo shiftInfo = createActiveShift();

        //when
        ProcessInstance session = shiftApplicationService.startShift(shiftInfo);
        List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();

        //then
        Assert.assertThat(session, is(notNullValue()));
        Assert.assertThat(externalTasks.size(), is(Matchers.not(0)));
    }

    @Test
    public void canCreateActiveShiftWithPerson() {
        //given
        ShiftInfo shiftInfo = createActiveShift();
        shiftInfo.setStaffId("person");

        //when
        ProcessInstance session = shiftApplicationService.startShift(shiftInfo);
        List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();

        //then
        Assert.assertThat(session, is(notNullValue()));
        Assert.assertThat(externalTasks.size(), is(Matchers.not(0)));
    }

    @Test
    public void canGetActiveShift() {
        //given
        ShiftInfo shiftInfo = createActiveShift();

        //and
        shiftApplicationService.startShift(shiftInfo);

        //when
        ShiftInfo loaded = shiftApplicationService.getShiftInfo(shiftInfo.getEmail());

        //then
        assertThat(loaded, is(notNullValue()));
    }


    @Test(expected = ResourceNotFound.class)
    public void canDeleteActiveShift() {

        //given
        ShiftInfo shiftInfo = createActiveShift();

        String email = shiftInfo.getEmail();
        wireMockRule.stubFor(delete(urlEqualTo("/shift?email=eq."+ email))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withStatus(200)));

        //and
        shiftApplicationService.startShift(shiftInfo);

        //when
        shiftApplicationService.deleteShift(email, "no need");

        //then
        shiftApplicationService.getShiftInfo(email);

    }

    private ShiftInfo createActiveShift() {
        ShiftInfo shiftInfo = new ShiftInfo();
        shiftInfo.setEmail("testEmail");
        shiftInfo.setStartDateTime(LocalDateTime.now().toDate());
        shiftInfo.setStaffId(UUID.randomUUID().toString());
        shiftInfo.setShiftHours(1);
        shiftInfo.setShiftMinutes(0);
        return shiftInfo;
    }
}
