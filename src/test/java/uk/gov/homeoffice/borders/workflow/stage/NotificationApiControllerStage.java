package uk.gov.homeoffice.borders.workflow.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.IdentityService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Assert;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.identity.UserService;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification;
import uk.gov.homeoffice.borders.workflow.task.notifications.Priority;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
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

    public NotificationApiControllerStage aNotification() {
        notification = new Notification();
        return this;
    }

    public NotificationApiControllerStage withName(String name) {
        Assert.notNull(notification, "Notification not initialised...call aNotification()");
        notification.setName(name);
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
        user.setUsername("test");
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);

        Mockito.when(userService.allUsers()).thenReturn(Collections.singletonList(user));

        mvcPostResult = mockMvc.perform(post("/api/workflow/notifications").content(objectMapper.writeValueAsString(notification))
                .contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    public NotificationApiControllerStage notificationWasSuccessful() throws Exception {
        mvcPostResult.andExpect(status().isOk());
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
        user.setUsername("test");
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
