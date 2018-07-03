package uk.gov.homeoffice.borders.workflow.task.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class TaskComment {

    @JsonProperty("taskcommentid")
    private String id;
    @JsonProperty("taskcomment")
    private String comment;
    @JsonProperty("createdon")
    private Date createdOn;
    @JsonProperty("taskid")
    private String taskId;
    @JsonProperty("staffid")
    private String staffId;
    private String email;
}
