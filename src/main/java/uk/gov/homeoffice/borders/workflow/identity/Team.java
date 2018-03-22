package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements org.camunda.bpm.engine.identity.Group {
    @JsonProperty("teamid")
    private String id;
    @JsonProperty("teamname")
    private String name;
    @JsonProperty("locationname")
    private String location;
    @JsonProperty("regionname")
    private String region;
    @JsonProperty("teamcode")
    private String teamCode;
    private String description;
    @JsonProperty("costcentrecode")
    private String costCentreCode;
    private List<Team> teams = new ArrayList<>();
    private String type;


    public static Stream<Team> flatten(Team team) {
        return Stream.concat(
                Stream.of(team),
                team.getTeams().stream().flatMap(Team::flatten));
    }
}
