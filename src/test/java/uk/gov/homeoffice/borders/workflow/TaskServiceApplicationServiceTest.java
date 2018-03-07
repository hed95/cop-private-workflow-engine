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
                .numberOfTasksShouldBe(1L)
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
                .numberOfTasksShouldBe(1L);
    }

    @Test
    public void canGetPagedTasks() {
        given()
                .aTask()
                .isCreated()
                .and()
                .withCandidateGroup("testCandidateC")
                .when()
                .getTaskForCandidateGroups("testCandidateC")
                .then()
                .numberOfPages(1)
                .and()
                .totalResultsIs(1L);
    }
}
