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
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

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

            ((ExecutionEntity) execution).getProcessDefinition().getCandidateStarterUserIdExpressions()
                    .forEach(expression -> {
                        if (!"".equalsIgnoreCase(expression.getExpressionText())) {
                            String user = expressionManager
                                    .createExpression(expression.getExpressionText()).getValue(execution).toString();
                            if (!"".equalsIgnoreCase(user)) {
                                Authorization newAuthorization = authorizationService
                                        .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                                newAuthorization.setResource(Resources.PROCESS_INSTANCE);
                                newAuthorization.setUserId(user);
                                newAuthorization.setResourceId(execution.getProcessInstanceId());
                                newAuthorization.addPermission(ACCESS);
                                authorizationService.saveAuthorization(newAuthorization);
                            }
                        }
                    });

            ((ExecutionEntity) execution).getProcessDefinition().getCandidateStarterGroupIdExpressions()
                    .forEach(expression -> {
                        if (!"".equalsIgnoreCase(expression.getExpressionText())) {
                            String group = expressionManager
                                    .createExpression(expression.getExpressionText()).getValue(execution).toString();
                            if (!"".equalsIgnoreCase(group)) {
                                Authorization newAuthorization = authorizationService
                                        .createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                                newAuthorization.setResource(Resources.PROCESS_INSTANCE);
                                newAuthorization.setGroupId(group);
                                newAuthorization.setResourceId(execution.getProcessInstanceId());
                                newAuthorization.addPermission(ACCESS);
                                authorizationService.saveAuthorization(newAuthorization);
                            }
                        }
                    });


        } catch (Exception e) {
            log.error("Failed to generate authorization for process instance", e);
        }

    }
}
