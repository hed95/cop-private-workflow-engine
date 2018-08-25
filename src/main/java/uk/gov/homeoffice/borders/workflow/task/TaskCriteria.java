package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;
import org.camunda.bpm.engine.task.TaskQuery;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.validation.constraints.NotNull;

import static java.util.stream.Collectors.toList;

@Data
class TaskCriteria {

    private Boolean teamOnly;
    private Boolean unassignedOnly;
    private Boolean assignedToMeOnly;

}
