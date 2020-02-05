package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Case extends ResourceSupport {

    private String businessKey;
    private List<HistoricProcessInstanceDto> associatedProcessInstances = new ArrayList<>();

}
