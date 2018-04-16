package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;
import uk.gov.homeoffice.borders.workflow.stage.TaskApplicationServiceStage;

public class TaskServiceApplicationServiceTest extends JGivenBaseTestClass<TaskApplicationServiceStage> {


    @Test
    public void canGetTasksForUsername() {
        given().aTask()
                .withUsername("username")
                .isCreated()
                .when()
                .getTasksForUserIsRequested("username")
                .then()
                .numberOfTasksShouldBe(1L)
                .and()
                .assignedTasksUsernameIs("username");
    }

    @Test
    public void canGetTasksForCandidateGroups() {
        given().aTask()
                .withCandidateGroup("testCandidateA")
                .isCreated()
                .and()
                .when()
                .getTaskForCandidateGroups("testCandidateA")
                .then()
                .numberOfTasksShouldBe(1L);
    }

    @Test
    public void canGetPagedTasks() {
        given()
                .aTask()
                .withCandidateGroup("testCandidateC")
                .isCreated()
                .and()
                .when()
                .getTaskForCandidateGroups("testCandidateC")
                .then()
                .numberOfPages(1)
                .and()
                .totalResultsIs(1L);
    }
}
