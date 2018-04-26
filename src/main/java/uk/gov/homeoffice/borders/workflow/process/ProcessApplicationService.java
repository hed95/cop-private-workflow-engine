package uk.gov.homeoffice.borders.workflow.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.task.notifications.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessApplicationService {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private FormService formService;

    public Page<ProcessDefinition> processDefinitions(User user, Pageable pageable) {
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .list();

        //TODO: Filter by team
//        List<String> teamIds = Team.flatten(user.getTeam()).map(Team::getTeamCode).collect(Collectors.toList());
        List<ProcessDefinition> definitions = processDefinitions.stream()
                .filter(p -> !p.getKey().equalsIgnoreCase("activate-session")
                        && !p.getKey().equalsIgnoreCase("notifications")).collect(Collectors.toList());

        return new PageImpl<>(definitions, new PageRequest(pageable.getPageNumber(), pageable.getPageSize()), definitions.size());


    }

    public String formKey(String processDefinitionId) {
        String startFormKey = formService.getStartFormKey(processDefinitionId);
        if (startFormKey == null) {
            throw new ResourceNotFound(String.format("Process definition %s does not have a start form", processDefinitionId));
        }
        return startFormKey;
    }

    public void delete(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance '{}' deleted", processInstanceId);
    }


    public ProcessInstance createInstance(ProcessStartDto processStartDto, User user) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processStartDto.getProcessKey()).singleResult();
        if (processDefinition == null) {
            throw new ResourceNotFound("Process definition with key '" + processStartDto.getProcessKey() + "'");
        }
        ObjectValue dataObject =
                Variables.objectValue(processStartDto)
                        .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                        .create();

        Map<String, Object> variables = new HashMap<>();
        variables.put(processStartDto.getVariableName(), dataObject);
        variables.put("type", "non-notifications");

        return runtimeService.startProcessInstanceByKey(processStartDto.getProcessKey(),
                variables);

    }

    public ProcessInstance getProcessInstance(String processInstanceId, User user) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    public VariableMap variables(String processInstanceId, User user) {
        VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, false);
        return variables;
    }

    public ProcessDefinition getDefinition(String processKey) {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processKey).singleResult();
        if (processDefinition == null) {
            throw new ResourceNotFound("Definition does nto exist");
        }
        return processDefinition;
    }
}
