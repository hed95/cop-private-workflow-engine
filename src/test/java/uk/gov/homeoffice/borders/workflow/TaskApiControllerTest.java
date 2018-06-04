package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;
import uk.gov.homeoffice.borders.workflow.stage.TaskApiControllerStage;


public class TaskApiControllerTest extends JGivenBaseTestClass<TaskApiControllerStage> {


    @Test
    public void canGetPagedResults() throws Exception {
        given()
                .aNumberOfTasksCreated(30)
                .when()
                .aCallToGetTasksIsForUser("test")
                .then()
                .statusIsOK()
                .and()
                .resultSizeIsNotZero()
                .and()
                .hasNextLink()
                .and()
                .hasFirstLink()
                .and()
                .hasLastLink();
    }

    @Test
    public void canQueryByTaskName() throws Exception {
        given()
                .aNumberOfTasksCreated(1)
                .when()
                .aQueryWithTaskName("Perform duty for test 0")
                .then()
                .responseIsOK()
                .and()
                .numberOfResultsShouldGreaterOrEqualTo(1);
    }

    @Test
    public void canGetTaskCount() throws Exception {
        given()
                .aNumberOfTasksCreated(30)
                .when()
                .taskCountForUser("test")
                .then()
                .numberOfUnassignedTasks(0L)
                .and()
                .numberOfAssignedTasksToUser(30L)
                .and()
                .numberOfTasksAssignedToTeam(30L);
    }

}
