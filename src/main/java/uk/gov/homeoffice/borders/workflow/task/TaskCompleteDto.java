package uk.gov.homeoffice.borders.workflow.task;

import lombok.Data;

@Data
public class TaskCompleteDto {
    private String variableName;
    private Object data;
}
