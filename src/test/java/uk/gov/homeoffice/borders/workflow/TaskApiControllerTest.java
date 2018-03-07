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
                    .totalResults(30)
                .and()
                    .hasNextLink()
                .and()
                    .hasFirstLink()
                .and()
                    .hasLastLink();
    }

    @Test
    public void canClaimTask() throws Exception {

    }

    @Test
    public void canCompleteTask() throws Exception {

    }

    @Test
    public void cannotClaimTaskAsUserDoesNotOwnTask() {

    }

}
