package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification;

import java.util.*;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    private RestTemplate restTemplate;
    private String teamServiceEndpoint;

    public UserDetailDto userDetailDto(String id) {
        return restTemplate.getForEntity(teamServiceEndpoint + "/regions/users/{id}",
                UserDetailDto.class, Collections.singletonMap("id", id)).getBody();
    }

}
