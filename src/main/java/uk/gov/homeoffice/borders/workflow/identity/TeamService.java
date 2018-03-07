package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    private RestTemplate restTemplate;
    private String teamServiceEndpoint;

    public List<Team> findByUser(User user) {
        Map<String, String> variables = Collections.singletonMap("email", user.getEmail());

        ResponseEntity<List<Team>> response = restTemplate
                .exchange(teamServiceEndpoint + "/{email}", HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Team>>() {
                        }, variables);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Unable to find teams for user");
        }
        return response.getBody();
    }
}
