package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TeamService {

    private RestTemplate restTemplate;
    private RefDataUrlBuilder refDataUrlBuilder;

    public Team findById(String teamId) {
        TeamsDto teamsDto = restTemplate.getForEntity(refDataUrlBuilder.teamByCode(teamId), TeamsDto.class).getBody();
        return Optional.ofNullable(teamsDto).map(teams -> !teams.getData().isEmpty() ? teams.getData().get(0) : null)
               .orElse(null);
    }

    public List<Group> findByQuery(TeamQuery query) {
        TeamsDto teamsDto = restTemplate.getForEntity(refDataUrlBuilder.teamQuery(query), TeamsDto.class).getBody();;
        return new ArrayList<>(Optional.ofNullable(teamsDto).map(TeamsDto::getData).orElse(new ArrayList<>()));
    }
}
