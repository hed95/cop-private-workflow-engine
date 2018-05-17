package uk.gov.homeoffice.borders.workflow.identity;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.List;

public class TeamQuery extends GroupQueryImpl {

    public TeamQuery() {
        super();
    }

    public TeamQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        final CustomIdentityProvider provider = getIdentityProvider(commandContext);
        return provider.findGroupCountByQueryCriteria(this);
    }

    @Override
    public List<Group> executeList(CommandContext commandContext, Page page) {
        return getIdentityProvider(commandContext).findGroupByQueryCriteria(this);
    }

    private CustomIdentityProvider getIdentityProvider(CommandContext commandContext) {
        return (CustomIdentityProvider) commandContext.getReadOnlyIdentityProvider();
    }
}
