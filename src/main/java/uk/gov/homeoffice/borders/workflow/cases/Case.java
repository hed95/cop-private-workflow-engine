package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Data;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class Case {

    private String businessKey;
    private List<HistoricProcessInstanceDto> associatedProcessInstances = new ArrayList<>();

}
