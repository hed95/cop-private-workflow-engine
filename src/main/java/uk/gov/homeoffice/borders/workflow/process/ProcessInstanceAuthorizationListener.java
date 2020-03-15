package uk.gov.homeoffice.borders.workflow.process;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.cmd.GetIdentityLinksForProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.task.IdentityLink;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;

@Slf4j
@AllArgsConstructor
public class ProcessInstanceAuthorizationListener implements ExecutionListener {

    private AuthorizationService authorizationService;


    @Override
    public void notify(DelegateExecution execution) throws Exception {
        try {
            ExpressionManager expressionManager = ((SpringProcessEngineConfiguration)
                    execution.getProcessEngine().getProcessEngineConfiguration()).getExpressionManager();

            List<IdentityLinkEntity> identityLinks =
                    ((ExecutionEntity) execution).getProcessDefinition().getIdentityLinks();

            if (!identityLinks.isEmpty()) {
                List<String> candidateGroups = identityLinks.stream()
                        .map(IdentityLink::getGroupId)
                        .filter(Objects::nonNull)
                        .map(i -> expressionManager.createExpression(i).getValue(execution).toString())
                        .collect(toList());

                if (!candidateGroups.isEmpty()) {
                    candidateGroups.forEach(i -> applyAuthorization(execution, i, false));
                }

                List<String> candidateUsers = identityLinks.stream()
                        .map(IdentityLink::getUserId)
                        .filter(Objects::nonNull)
                        .map(i -> expressionManager.createExpression(i).getValue(execution).toString())
                        .collect(toList());


                if (!candidateUsers.isEmpty()) {
                    candidateUsers.forEach(i -> applyAuthorization(execution, i, true));
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate authorization for process instance", e);
        }

    }

    private void applyAuthorization(DelegateExecution execution, String identity, boolean isUser) {
        Authorization newAuthorization = authorizationService
                .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        newAuthorization.setResource(Resources.PROCESS_INSTANCE);
        if (isUser) {
            newAuthorization.setUserId(identity);
        } else {
            newAuthorization.setGroupId(identity);
        }
        newAuthorization.setResourceId(execution.getProcessInstanceId());
        newAuthorization.addPermission(ACCESS);
        authorizationService.saveAuthorization(newAuthorization);
    }
}
