package uk.gov.homeoffice.borders.workflow;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HealthControllerTest extends BaseTestClass {

    @Autowired
    private TestRestTemplate testRestTemplate;


    @Test
    public void canGetResponseForReadiness() {
        //when
        String response = testRestTemplate.getForEntity("/api/engine", String.class).getBody();

        //then
        assertThat(response, is("borders"));
    }

}
