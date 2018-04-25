package uk.gov.homeoffice.borders.workflow.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.IdentityService;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Assert;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.identity.UserService;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification;
import uk.gov.homeoffice.borders.workflow.task.notifications.Priority;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@JGivenStage
public class NotificationApiControllerStage extends Stage<NotificationApiControllerStage> {

    private Notification notification;

    private ResultActions mvcPostResult;

    private ResultActions mvcGetResult;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationClient notificationClient;

    public NotificationApiControllerStage aNotification() {
        notification = new Notification();
        notification.setTeam("teamA");
        return this;
    }

    public NotificationApiControllerStage withSubject(String name) {
        Assert.notNull(notification, "Notification not initialised...call aNotification()");
        notification.setSubject(name);
        return this;
    }

    public NotificationApiControllerStage payloadOf(Object payload) {
        Assert.notNull(notification, "Notification not initialised...call aNotification()");
        notification.setPayload(payload);
        return this;
    }

    public NotificationApiControllerStage regionOf(String region) {
        Assert.notNull(notification, "Notification not initialised...call aNotification()");
        notification.setRegion(region);
        return this;
    }

    public NotificationApiControllerStage notificationIsPosted() throws Exception {

        User user = new User();
        user.setEmail("test");
        Team team = new Team();
        team.setName("teamA");
        team.setTeamCode("teamA");
        user.setTeam(team);


        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);

        Mockito.when(userService.allUsers()).thenReturn(Collections.singletonList(user));

        String asString = objectMapper.writeValueAsString(notification);
        mvcPostResult = mockMvc.perform(post("/api/workflow/notifications").content(asString)
                .contentType(MediaType.APPLICATION_JSON));

        return this;
    }

    public NotificationApiControllerStage notificationWasSuccessful() throws Exception {
        mvcPostResult.andExpect(status().isCreated());
        return this;
    }

    public NotificationApiControllerStage priority() {
        Priority priority = new Priority();
        notification.setPriority(priority);
        return this;
    }

    public NotificationApiControllerStage ofUrgent() {
        notification.getPriority().setType(Priority.Type.URGENT);
        return this;
    }

    public NotificationApiControllerStage aRequestForNotificationForUserIsMade() throws Exception {
        User user = new User();
        user.setEmail("test");
        Team team = new Team();
        team.setName("teamA");
        team.setTeamCode("teamA");
        user.setTeam(team);
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);

        mvcGetResult = mockMvc.perform(get("/api/workflow/notifications")
                .contentType(MediaType.APPLICATION_JSON));

        return this;
    }


    public NotificationApiControllerStage responseIsSuccessful() throws Exception {
        mvcGetResult.andExpect(status().isOk());
        return this;
    }

    public NotificationApiControllerStage numberOfNotificationsIs(int expected) throws Exception {
        mvcGetResult.andExpect(jsonPath("$.page['totalElements']", is(expected)));
        return this;
    }
}
