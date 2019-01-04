package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.task.TaskQuery;

import java.util.Date;

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


    void apply(TaskQuery taskQuery) {
        if (StringUtils.isNotBlank(name)) {
            taskQuery.taskNameLike("%" + name + "%");
        }
    }
}
