package uk.gov.homeoffice.borders.workflow.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.task.notifications.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessApplicationService {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private FormService formService;

    public List<ProcessDefinition> processDefinitions(User user) {
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .list();

        //TODO: Filter by team
        List<String> teamIds = Team.flatten(user.getTeam()).map(Team::getName).collect(Collectors.toList());

        return processDefinitions.stream().filter(p -> !p.getKey().equalsIgnoreCase(NotificationService.NOTIFICATIONS)
                || !p.getKey().equalsIgnoreCase("session")).collect(Collectors.toList());
    }

    public String formKey(String processDefinitionKey) {
        String startFormKey = formService.getStartFormKey(processDefinitionKey);
        if (startFormKey == null) {
            throw new ResourceNotFound(String.format("Process definition %s does not have a start form", processDefinitionKey));
        }
        return startFormKey;
    }

    public void delete(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance '{}' deleted", processInstanceId);
    }


}
