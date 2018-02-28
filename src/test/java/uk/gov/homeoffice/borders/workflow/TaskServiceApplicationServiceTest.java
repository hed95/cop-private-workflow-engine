package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;
import uk.gov.homeoffice.borders.workflow.stage.TaskApplicationServiceStage;

public class TaskServiceApplicationServiceTest extends BaseTestClass<TaskApplicationServiceStage> {


    @Test
    public void canGetTasksForUsername() {
        given().aTask()
                .withUsername("username")
                .isCreated()
                .when()
                .getTasksForUserIsRequested("username")
                .then()
                .numberOfTasksShouldBe(1)
                .and()
                .assignedTasksUsernameIs("username");
    }

    @Test
    public void canGetTasksForCandidateGroups() {
        given().aTask()
                .isCreated()
                .and()
                .withCandidateGroup("testCandidateA")
                .when()
                .getTaskForCandidateGroups("testCandidateA")
                .then()
                .numberOfTasksShouldBe(1);
    }
}
