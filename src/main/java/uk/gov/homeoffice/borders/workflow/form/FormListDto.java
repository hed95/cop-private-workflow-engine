package uk.gov.homeoffice.borders.workflow.form;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FormListDto {

    private long total;
    private List<FormDto> forms = new ArrayList<>();
}
