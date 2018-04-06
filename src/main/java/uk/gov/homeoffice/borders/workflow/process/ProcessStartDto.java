package uk.gov.homeoffice.borders.workflow.process;

import lombok.Data;

@Data
public class ProcessStartDto {

    private String processKey;
    private String variableName;
    private Object data;
}
