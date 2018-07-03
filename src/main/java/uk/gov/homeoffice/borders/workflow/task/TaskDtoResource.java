package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import java.util.List;
import java.util.Map;

@Data
@Relation(collectionRelation = "tasks")
@EqualsAndHashCode(callSuper = false)
public class TaskDtoResource extends ResourceSupport {

    @JsonProperty("task")
    private TaskDto taskDto;
    @JsonProperty("candidateGroups")
    private List<String> candidateGroups;
    private Map<String, VariableValueDto> variables;

}
