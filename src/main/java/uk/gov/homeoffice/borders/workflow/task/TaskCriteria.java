package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;
import org.camunda.bpm.engine.task.TaskQuery;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.validation.constraints.NotNull;

import java.util.Date;

import static java.util.stream.Collectors.toList;

@Data
class TaskCriteria {

    private Boolean teamOnly;
    private Boolean unassignedOnly;
    private Boolean assignedToMeOnly;
    private String name;
    private String assignee;
    private Date dueBefore;
    private Date dueAfter;
    private Date createdBefore;
    private Date createdAfter;

}
