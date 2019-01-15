package uk.gov.homeoffice.borders.workflow.process;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class ProcessStartDto {

    @NotNull @NotBlank
    private String processKey;
    @NotNull @NotBlank
    private String variableName;
    @NotNull
    private Object data;
    private Optional<String> businessKey;
}


