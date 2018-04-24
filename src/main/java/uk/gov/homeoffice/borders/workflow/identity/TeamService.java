package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.Group;
import org.json.JSONArray;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    private RestTemplate restTemplate;
    private String prestUrl;
    private ObjectMapper objectMapper;

    public Team findById(String teamId) {

        List<Team> filteredTeam = allTeams().stream().filter(t -> t.getTeamCode().equalsIgnoreCase(teamId)
                || t.getName().equalsIgnoreCase(teamId)
                || t.getId().equalsIgnoreCase(teamId)).collect(toList());
        if (CollectionUtils.isEmpty(filteredTeam)) {
            throw new ResourceNotFound("Team with id '" + teamId + "' could not be found");
        }
        return filteredTeam.get(0);
    }


    public List<Team> allTeams() {
        String response = restTemplate.getForEntity(String.format("%s/_QUERIES/read/get-teams",
                prestUrl), String.class).getBody();
        JSONArray o = new JSONArray(response);
        if (o.length() == 0) {
            throw new ForbiddenException("No active user found in store");
        }
        if (o.length() > 1) {
            throw new ForbiddenException("Cannot have multiple active session for user");
        }
        try {
            return objectMapper.readValue(o.getJSONObject(0).get("array_to_json").toString(), new TypeReference<List<Team>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Group> findByQuery(TeamQuery query) {
        List<Team> teams = allTeams();

        if (query.getId() != null) {
            teams.removeIf(t -> !query.getId().equalsIgnoreCase(t.getId()));
        }
        if (query.getName() != null) {
            teams.removeIf(t -> !query.getName().equalsIgnoreCase(t.getName()));
        }
        return new ArrayList<>(teams);
    }
}
