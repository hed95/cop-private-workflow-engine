package uk.gov.homeoffice.borders.workflow.process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@Relation(collectionRelation = "process-definitions")
@EqualsAndHashCode(callSuper = false)
public class ProcessDefinitionDtoResource extends RepresentationModel<ProcessDefinitionDtoResource> {

    @JsonProperty("process-definition")
    private ProcessDefinitionDto processDefinitionDto;
    private String formKey;

}
