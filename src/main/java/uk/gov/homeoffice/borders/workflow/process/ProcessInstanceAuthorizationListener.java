package uk.gov.homeoffice.borders.workflow.process;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

import java.util.List;
import java.util.stream.Collectors;

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
                    ((ExecutionEntity) execution).getProcessDefinition().getIdentityLinks()
                            .stream()
                            .filter(i -> i.isUser() || i.isGroup()).collect(Collectors.toList());

            if (!identityLinks.isEmpty()) {

                for (IdentityLinkEntity identityLink : identityLinks) {
                    String group = null;
                    String user = null;
                    if (identityLink.isGroup()) {
                        group = expressionManager
                                .createExpression(identityLink.getGroupId()).getValue(execution).toString();

                        if (!"".equalsIgnoreCase(group)) {
                            long authorization = authorizationService.createAuthorizationQuery()
                                    .resourceType(Resources.PROCESS_INSTANCE)
                                    .resourceId(execution.getProcessInstanceId())
                                    .groupIdIn(group)
                                    .count();
                            if (authorization == 0) {
                                Authorization newAuthorization = authorizationService
                                        .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                                newAuthorization.setResource(Resources.PROCESS_INSTANCE);
                                newAuthorization.setGroupId(group);
                                newAuthorization.setResourceId(execution.getProcessInstanceId());
                                newAuthorization.addPermission(ACCESS);
                                authorizationService.saveAuthorization(newAuthorization);
                            }
                        }

                    }
                    if (identityLink.isUser()) {
                        user = expressionManager
                                .createExpression(identityLink.getUserId()).getValue(execution).toString();
                        if (!"".equalsIgnoreCase(user)) {
                            long authorization = authorizationService.createAuthorizationQuery()
                                    .resourceType(Resources.PROCESS_INSTANCE)
                                    .resourceId(execution.getProcessInstanceId())
                                    .userIdIn(user)
                                    .count();
                            if (authorization == 0) {
                                Authorization newAuthorization = authorizationService
                                        .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                                newAuthorization.setResource(Resources.PROCESS_INSTANCE);
                                newAuthorization.setUserId(user);
                                newAuthorization.setResourceId(execution.getProcessInstanceId());
                                newAuthorization.addPermission(ACCESS);
                                authorizationService.saveAuthorization(newAuthorization);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate authorization for process instance", e);
        }

    }
}
