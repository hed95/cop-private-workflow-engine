package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;
import uk.gov.homeoffice.borders.workflow.stage.NotificationApiControllerStage;

public class NotificationApiControllerTest extends JGivenBaseTestClass<NotificationApiControllerStage> {

    @Test
    public void canCreateNotifications() throws Exception {
            given()
                .aNotification()
                    .withSubject("Alert")
                .and()
                    .payloadOf("Some payload")
                .and()
                    .regionOf("South")
                .and()
                    .priority().ofUrgent()
                .when()
                    .notificationIsPosted()
                .then()
                    .notificationWasSuccessful();
    }

    @Test
    public void canGetNotificationsForUser() throws Exception {
        given()
                .aNotification()
                    .withSubject("Alert")
                .and()
                    .payloadOf("Some payload")
                .and()
                    .regionOf("South")
                .and()
                    .priority().ofUrgent()
                .and()
                    .notificationIsPosted()
                .when()
                    .aRequestForNotificationForUserIsMade()
                .then()
                    .responseIsSuccessful()
                .and()
                    .numberOfNotificationsIs(1);
    }
}
