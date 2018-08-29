package uk.gov.homeoffice.borders.workflow.task.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class TaskComment {

    @JsonProperty("taskcommentid")
    private String id;
    @JsonProperty("taskcomment")
    @NotBlank
    @NotNull
    private String comment;
    @JsonProperty("createdon")
    private Date createdOn;
    @JsonProperty("taskid")
    @NotBlank @NotNull
    private String taskId;
    @JsonProperty("staffid")
    private String staffId;
    private String email;
}
