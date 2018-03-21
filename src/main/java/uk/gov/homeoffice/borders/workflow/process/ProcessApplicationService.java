package uk.gov.homeoffice.borders.workflow.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessApplicationService {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;

    public List<ProcessDefinition> processDefinitions(User user) {
        List<String> teamIds = Team.flatten(user.getTeam()).map(Team::getName).collect(Collectors.toList());
        return new ArrayList<>();
    }

    public void delete(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance '{}' deleted", processInstanceId);
    }


}
