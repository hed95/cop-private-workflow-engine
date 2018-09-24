package uk.gov.homeoffice.borders.workflow.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.spin.Spin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.PageHelper;
import uk.gov.homeoffice.borders.workflow.exception.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
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
    private JacksonJsonDataFormat formatter;
    private static final PageHelper PAGE_HELPER = new PageHelper();

    /**
     * Returns the process definitions based on user qualifications and grades.
     *
     * @param user
     * @param pageable
     * @return paged result
     */
    public Page<ProcessDefinition> processDefinitions(@NotNull ShiftUser user, Pageable pageable) {
        log.debug("Loading process definitions for '{}'", user.getEmail());
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .list();
        List<ProcessDefinition> definitions = processDefinitions.stream()
                .filter(p -> !p.getKey().equalsIgnoreCase("activate-shift")
                        && !p.getKey().equalsIgnoreCase("notifications"))
                .filter(ProcessDefinition::hasStartFormKey)
                .sorted(Comparator.comparing(ResourceDefinition::getName))
                .collect(Collectors.toList());

        return new PageImpl<>(definitions, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), definitions.size());


    }

    /**
     * Get form key for given process definition key
     * @param processDefinitionId
     * @return form key
     */
    public String formKey(String processDefinitionId) {
        return formService.getStartFormKey(processDefinitionId);
    }

    public void delete(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance '{}' deleted", processInstanceId);
    }


    public ProcessInstance createInstance(@NotNull ProcessStartDto processStartDto, @NotNull ShiftUser user) {
        ProcessDefinition processDefinition = getDefinition(processStartDto.getProcessKey());

        Spin<?> spinObject = Spin.S(processStartDto.getData(), formatter);

        Map<String, Object> variables = new HashMap<>();
        variables.put(processStartDto.getVariableName(), spinObject);
        variables.put("type", "non-notifications");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(),
                variables);
        log.info("'{}' was successfully started with id '{}' by '{}'", processStartDto.getProcessKey(),
                processInstance.getProcessInstanceId(), user.getEmail());

        return processInstance;

    }

    public ProcessInstance getProcessInstance(@NotNull String processInstanceId, @NotNull ShiftUser user) {
        log.info("ShiftUser '{}' requested process instance '{}'", user.getEmail(), processInstanceId);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ResourceNotFound("Process instance not found");
        }
        return processInstance;
    }

    public VariableMap variables(String processInstanceId, @NotNull ShiftUser user) {
        log.info("ShiftUser '{}' requested process instance variables for '{}'", user.getEmail(), processInstanceId);
        return runtimeService.getVariablesTyped(processInstanceId, false);
    }

    public ProcessDefinition getDefinition(String processKey) {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .processDefinitionKey(processKey).singleResult();
        if (processDefinition == null) {
            throw new ResourceNotFound(String.format("%s definition does not exist in workflow engine", processKey));
        }
        return processDefinition;
    }
}
