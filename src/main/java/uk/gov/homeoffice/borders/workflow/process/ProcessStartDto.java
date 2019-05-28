package uk.gov.homeoffice.borders.workflow.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ProcessStartDto {

    @NotNull @NotBlank
    @ApiModelProperty(required = true)
    private String processKey;
    @ApiModelProperty(required = true)
    @NotNull @NotBlank
    private String variableName;
    @NotNull
    @ApiModelProperty(required = true)
    private Object data;
    private String businessKey;
}


