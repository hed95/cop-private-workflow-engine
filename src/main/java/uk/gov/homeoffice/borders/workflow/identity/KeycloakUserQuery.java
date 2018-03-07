package uk.gov.homeoffice.borders.workflow.identity;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.List;

public class KeycloakUserQuery extends UserQueryImpl {

    KeycloakUserQuery() {
        super();
    }

    KeycloakUserQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        final KeycloakIdentityProvider provider = getIdentityProvider(commandContext);
        return provider.findUserCountByQueryCriteria(this);
    }

    @Override
    public List<User> executeList(CommandContext commandContext, Page page) {
        return getIdentityProvider(commandContext).findUserByQueryCriteria(this);
    }

    private KeycloakIdentityProvider getIdentityProvider(CommandContext commandContext) {
        return (KeycloakIdentityProvider) commandContext.getReadOnlyIdentityProvider();
    }
}
