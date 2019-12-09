package uk.gov.homeoffice.borders.workflow.process;

import io.digitalpatterns.camunda.encryption.ProcessDefinitionEncryptionParser;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptor;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.spin.Spin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.homeoffice.borders.workflow.PageHelper;
import uk.gov.homeoffice.borders.workflow.exception.ResourceNotFound;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ProcessApplicationService {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private FormService formService;
    private JacksonJsonDataFormat formatter;
    private AuthorizationService authorizationService;
    private ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor;
    private ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor;
    private ProcessDefinitionEncryptionParser processDefinitionEncryptionParser;
    private TaskService taskService;

    private static final PageHelper PAGE_HELPER = new PageHelper();

    public List<ProcessDefinition> getDefinitions(List<String> processDefinitionIds) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionIdIn(processDefinitionIds.toArray(new String[]{})).list();
    }

    Page<ProcessDefinition> processDefinitions(@NotNull PlatformUser user, Pageable pageable) {
        log.debug("Loading process definitions for '{}'", user.getEmail());

        if (CollectionUtils.isEmpty(user.getShiftDetails().getRoles())) {
            log.info("Could not find any process definition authorizations based on user roles");
            return new PageImpl<>(new ArrayList<>(),
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), 0);
        }
        log.info("User '{}' current roles {}", user.getEmail(), user.getShiftDetails().getRoles());

        String[] processDefinitionIds = authorizationService.createAuthorizationQuery()
                .groupIdIn(user.getShiftDetails().getRoles().toArray(new String[]{}))
                .resourceType(Resources.PROCESS_DEFINITION)
                .list()
                .stream()
                .map(Authorization::getResourceId)
                .toArray(String[]::new);

        log.info("Process definitions based on authorizations {} ", asList(processDefinitionIds));

        List<ProcessDefinition> definitions = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKeysIn(processDefinitionIds)
                .latestVersion()
                .active()
                .listPage(PAGE_HELPER.calculatePageNumber(pageable), pageable.getPageSize())
                .stream()
                .filter((p) -> StringUtils.isNotBlank(p.getName()))
                .filter(ProcessDefinition::hasStartFormKey)
                .sorted(Comparator.comparing(ProcessDefinition::getName))
                .collect(Collectors.toList());

        return new PageImpl<>(definitions, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), definitions.size());


    }

    /**
     * Get form key for given process definition key
     *
     * @param processDefinitionId
     * @return form key
     */
    String formKey(String processDefinitionId) {
        return formService.getStartFormKey(processDefinitionId);
    }

    void delete(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance '{}' deleted", processInstanceId);
    }


    public Tuple2<ProcessInstance, List<Task>> createInstance(@NotNull ProcessStartDto processStartDto, @NotNull PlatformUser user) {
        ProcessDefinition processDefinition = getDefinition(processStartDto.getProcessKey());
        Map<String, Object> variables = new HashMap<>();
        variables.put("type", "non-notifications");
        variables.put("initiatedBy", user.getEmail());
        if (processDefinitionEncryptionParser.shouldEncrypt(processStartDto.getProcessKey(),
                "encryptVariables")) {
            variables.put(processStartDto.getVariableName(), processInstanceSpinVariableEncryptor.encrypt(
                    processStartDto.getData()
            ));
        } else {
            Spin<?> spinObject = Spin.S(processStartDto.getData(), formatter);
            variables.put(processStartDto.getVariableName(), spinObject);
        }

        ProcessInstance processInstance;

        if (StringUtils.isNotBlank(processStartDto.getBusinessKey())) {
            processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(),
                    processStartDto.getBusinessKey(),
                    variables);
        } else {
            processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(),
                    variables);
        }
        log.info("'{}' was successfully started with id '{}' by '{}'", processStartDto.getProcessKey(),
                processInstance.getProcessInstanceId(), user.getEmail());

        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(processInstance.getProcessInstanceId())
                .listPage(0, 1);

        if (processInstances != null && !processInstances.isEmpty()) {

            ExecutionEntity subProcessInstance = (ExecutionEntity)processInstances.get(0);
            VariableMap variableMap = new VariableMapImpl();
            runtimeService.createVariableInstanceQuery()
                    .processInstanceIdIn(subProcessInstance.getProcessInstanceId())
                    .list()
                    .forEach(
                            (v) -> variableMap.putValueTyped(v.getName(), v.getTypedValue())
                    );

            ProcessInstanceWithVariablesImpl withVariables = new ProcessInstanceWithVariablesImpl(
                    subProcessInstance, variableMap);
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId())
                    .taskAssignee(user.getEmail())
                    .initializeFormKeys().list();
            return Tuple.of(withVariables, tasks);
        }

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId())
                .taskAssignee(user.getEmail())
                .initializeFormKeys().list();

        return Tuple.of(processInstance, tasks);


    }

    ProcessInstance getProcessInstance(@NotNull String processInstanceId, @NotNull PlatformUser user) {
        log.info("PlatformUser '{}' requested process instance '{}'", user.getEmail(), processInstanceId);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ResourceNotFound("Process instance not found");
        }
        return processInstance;
    }

    public VariableMap variables(String processInstanceId, @NotNull PlatformUser user) {
        log.info("PlatformUser '{}' requested process instance variables for '{}'", user.getEmail(), processInstanceId);
        if (hasEncryption(processInstanceId)) {
            return processInstanceSpinVariableDecryptor.decrypt(runtimeService.getVariables(processInstanceId));
        }
        return runtimeService.getVariablesTyped(processInstanceId, false);
    }

    ProcessDefinition getDefinition(String processKey) {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .processDefinitionKey(processKey).singleResult();
        if (processDefinition == null) {
            throw new ResourceNotFound(String.format("%s definition does not exist in workflow engine", processKey));
        }
        return processDefinition;
    }

    private boolean hasEncryption(String processInstanceId) {
        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
        return processDefinitionEncryptionParser.shouldEncrypt(processDefinition,
                "encryptVariables");
    }

}
