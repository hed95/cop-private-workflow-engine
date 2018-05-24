package uk.gov.homeoffice.borders.workflow.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements org.camunda.bpm.engine.identity.Group {
    @JsonProperty("teamid")
    private String id;
    @JsonProperty("teamname")
    private String name;
    @JsonProperty("teamcode")
    private String teamCode;
    private String type;
}
