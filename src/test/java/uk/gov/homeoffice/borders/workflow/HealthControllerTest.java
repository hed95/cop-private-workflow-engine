package uk.gov.homeoffice.borders.workflow;

import com.tngtech.jgiven.annotation.As;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.homeoffice.borders.workflow.stage.HealthStage;

public class HealthControllerTest extends JGivenBaseTestClass<HealthStage> {


    @Test
    @As("Engine endpoint returns correct engine name")
    public void canGetResponseForReadiness() throws Exception {
       when().gettingEngineEndpoint()
               .then()
               .statusIs(HttpStatus.OK)
               .and()
               .engineNameIs("borders");
    }

}
