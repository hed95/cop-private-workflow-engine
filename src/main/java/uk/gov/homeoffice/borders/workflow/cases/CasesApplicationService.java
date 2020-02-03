package uk.gov.homeoffice.borders.workflow.cases;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.PageHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CasesApplicationService {

    private HistoryService historyService;

    private static final PageHelper PAGE_HELPER = new PageHelper();

    public Page<Case> findBy(String businessKey, Pageable pageable) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKeyLike(businessKey);

        long totalResults = query.count();

        List<HistoricProcessInstance> historicProcessInstances = query
                .listPage(PAGE_HELPER.calculatePageNumber(pageable)
                        ,pageable.getPageSize());

        Map<String, List<HistoricProcessInstance>> groupedByBusinessKey = historicProcessInstances
                .stream().collect(Collectors.groupingBy(HistoricProcessInstance::getBusinessKey));

        List<Case> cases = groupedByBusinessKey.keySet().stream().map((key) -> {
            Case caseDto = new Case();
            caseDto.setBusinessKey(key);
            List<HistoricProcessInstance> instances = groupedByBusinessKey.get(key);
            caseDto.setAssociatedProcessInstances(instances
                    .stream()
                    .map(HistoricProcessInstanceDto::fromHistoricProcessInstance).collect(Collectors.toList()));
            return caseDto;
        }).collect(Collectors.toList());

        log.info("Number of cases returned for '{}' is '{}'", businessKey, cases.size());
        return new PageImpl<>(cases, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), totalResults);

    }




}
