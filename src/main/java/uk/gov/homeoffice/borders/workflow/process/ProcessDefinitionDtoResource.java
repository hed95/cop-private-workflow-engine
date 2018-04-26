package uk.gov.homeoffice.borders.workflow.process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

@Data
@Relation(collectionRelation = "process-definitions")
@EqualsAndHashCode(callSuper = false)
class ProcessDefinitionDtoResource extends ResourceSupport {

    @JsonProperty("process-definition")
    private ProcessDefinitionDto processDefinitionDto;
    private String formKey;

}
