package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;

@Data
public class TasksCountDto {

    private Long tasksAssignedToUser;
    private Long totalTasksAllocatedToTeam;
    private Long tasksUnassigned;
}
