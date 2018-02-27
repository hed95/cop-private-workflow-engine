package uk.gov.homeoffice.borders.workflow.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@JGivenStage
public class HealthStage extends Stage<HealthStage> {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private ResponseEntity response;

    public HealthStage gettingEngineEndpoint() throws Exception {
        response = testRestTemplate.getForEntity("/engine", Map.class);
        return this;
    }

    public HealthStage statusIs( HttpStatus status ) throws Exception {
        assertThat(response.getStatusCode(), is(status));
        return this;
    }


    public HealthStage engineNameIs( @Quoted String content ) throws Exception {
        assertThat(((Map)response.getBody()).get("engine"), is(content));
        return this;
    }

}

