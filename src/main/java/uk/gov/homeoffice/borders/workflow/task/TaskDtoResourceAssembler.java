package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class TaskDtoResourceAssembler implements ResourceAssembler<Task, TaskDtoResource> {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;


    @Override
    public TaskDtoResource toResource(Task task) {

        TaskEntity entity = (TaskEntity) task;
        TaskDto taskDto = TaskDto.fromEntity(entity);
        TaskDtoResource resource = new TaskDtoResource();
        resource.setTaskDto(taskDto);

        resource.setExtensionData(ofNullable(entity.getProcessDefinitionId())
                .map(definition -> extensionFrom(entity))
                .orElse(new HashMap<>()));


        return resource;
    }

    private Map<String, String> extensionFrom(TaskEntity entity) {
        String processDefinitionId = entity.getProcessDefinitionId();
        BpmnModelInstance bpmnModelInstance = processEngineConfiguration.getDeploymentCache()
                .findBpmnModelInstanceForProcessDefinition(processDefinitionId);

        if (bpmnModelInstance == null) {
            bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        }
        UserTask userTask = bpmnModelInstance.getModelElementById(entity.getTaskDefinitionKey());

        ExtensionElements extensionElements = userTask.getExtensionElements();

        return ofNullable(extensionElements).map(this::extract)
                .orElse(new HashMap<>());

    }

    private Map<String, String> extract(ExtensionElements extensionElements) {
        final Map<String,String> properties = new HashMap<>();
         extensionElements.getElementsQuery()
                .filterByType(CamundaProperties.class)
                .list().forEach(camundaProperties -> {
                 Map<String, String> props = camundaProperties.getCamundaProperties().stream()
                     .collect(Collectors.toMap(CamundaProperty::getCamundaName,
                             CamundaProperty::getCamundaValue));

             properties.putAll(props);
         });
        return properties;

    }
}
