package uk.gov.homeoffice.borders.workflow;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HealthControllerTest extends BaseTestClass {


    @Test
    public void canGetResponseForReadiness() {
        //when
        Map response = testRestTemplate.getForEntity("/engine", Map.class).getBody();

        //then
        assertThat(response.keySet().size(), is(1));
        assertThat(response.get("engine"), is("borders"));
    }

}
