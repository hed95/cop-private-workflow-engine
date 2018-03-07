package uk.gov.homeoffice.borders.workflow.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

@Data
@Relation(collectionRelation = "tasks")
public class TaskDtoResource extends ResourceSupport {

    @JsonProperty("task")
    private TaskDto taskDto;
}
