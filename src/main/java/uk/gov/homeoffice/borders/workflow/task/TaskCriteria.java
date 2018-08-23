package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;

@Data
class TaskCriteria {

    private Boolean teamOnly;
    private Boolean unassignedOnly;
    private Boolean assignedToMeOnly;

}
