package uk.gov.homeoffice.borders.workflow.identity;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.List;

public class UserQuery extends UserQueryImpl {

    private String location;
    private String command;
    private String subCommand;

    public UserQuery() {
        super();
    }

    UserQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        final CustomIdentityProvider provider = getIdentityProvider(commandContext);
        return provider.findUserCountByQueryCriteria(this);
    }

    @Override
    public List<User> executeList(CommandContext commandContext, Page page) {
        return getIdentityProvider(commandContext).findUserByQueryCriteria(this);
    }

    private CustomIdentityProvider getIdentityProvider(CommandContext commandContext) {
        return (CustomIdentityProvider) commandContext.getReadOnlyIdentityProvider();
    }

    public UserQuery location (String location) {
        this.location = location;
        return this;
    }

    public UserQuery command(String command) {
        this.command = command;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public String getCommand() {
        return command;
    }

    public UserQuery subCommand(String subCommandId) {
        this.subCommand = subCommandId;
        return null;
    }

    public String getSubCommand() {
        return subCommand;
    }
}
