package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * task information should return extension data as follows:
 * "extensionData":{"someKey2":"someValue2","someKey":"someValue"}
 */
@Data
@Relation(collectionRelation = "tasks")
@EqualsAndHashCode(callSuper = false)
public class TaskDtoResource extends RepresentationModel<TaskDtoResource> {

    @JsonProperty("task")
    private TaskDto taskDto;
    private List<String> candidateGroups;
    private Map<String, VariableValueDto> variables;
    @JsonProperty("process-definition")
    private ProcessDefinitionDto processDefinition;
    private Map<String,String> extensionData = new HashMap<>();
    private String businessKey;

}
