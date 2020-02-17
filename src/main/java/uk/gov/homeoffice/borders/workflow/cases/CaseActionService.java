package uk.gov.homeoffice.borders.workflow.cases;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;
import uk.gov.homeoffice.borders.workflow.process.ProcessDefinitionDtoResource;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CaseActionService {

    private RepositoryService repositoryService;
    private FormService formService;

    public List<CaseDetail.Action> getAvailableActions(CaseDetail caseDetail, PlatformUser platformUser) {
        CaseDetail.Action pdf = new CaseDetail.Action();
        pdf.setCompletionMessage("Your pdf will be sent to your email.");

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .processDefinitionKey("generate-case-pdf").singleResult();

        ProcessDefinitionDtoResource
                dtoResource = new ProcessDefinitionDtoResource();


        if (processDefinition.hasStartFormKey()) {
            String startFormKey = formService.getStartFormKey(processDefinition.getId());
            dtoResource.setFormKey(startFormKey);
        }

        dtoResource.setProcessDefinitionDto(ProcessDefinitionDto.fromProcessDefinition(processDefinition));
        pdf.setProcess(dtoResource);
        return Lists.newArrayList(pdf);
    }
}
