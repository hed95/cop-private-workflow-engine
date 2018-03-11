package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;
import uk.gov.homeoffice.borders.workflow.stage.TaskApiControllerStage;


public class TaskApiControllerTest extends BaseTestClass<TaskApiControllerStage> {


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
                .numberOfResultsShouldBe(1);
    }

}
