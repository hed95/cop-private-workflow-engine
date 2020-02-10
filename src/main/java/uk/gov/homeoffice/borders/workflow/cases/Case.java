package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "cases")
public class Case extends ResourceSupport {

    private String businessKey;
    private List<HistoricProcessInstanceDto> processInstances = new ArrayList<>();

}
