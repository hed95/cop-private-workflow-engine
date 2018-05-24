package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class CustomIdentityProvider implements ReadOnlyIdentityProvider {

    private UserService userService;
    private TeamService teamService;

    @Override
    public User findUserById(String userId) {
       return userService.findByUserId(userId);
    }

    @Override
    public org.camunda.bpm.engine.identity.UserQuery createUserQuery() {
        return new UserQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    }

    @Override
    public org.camunda.bpm.engine.identity.UserQuery createUserQuery(CommandContext commandContext) {
        return new UserQuery();
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        //Not required for this implementation
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        //Not required for this implementation
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public Team findGroupById(String groupId) {
        return teamService.findById(groupId);
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new TeamQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return new TeamQuery();
    }

    @Override
    public Tenant findTenantById(String tenantId) {
        return null;
    }

    @Override
    public TenantQuery createTenantQuery() {
        return null;
    }

    @Override
    public TenantQuery createTenantQuery(CommandContext commandContext) {
        return null;
    }

    @Override
    public void flush() {
        //Not required for this implementation
    }

    @Override
    public void close() {
        //Not required for this implementation
    }

    public long findUserCountByQueryCriteria(UserQuery query) {
        return findUserByQueryCriteria(query).size();

    }

    public List<org.camunda.bpm.engine.identity.User> findUserByQueryCriteria(UserQuery query) {
        List<User> users = userService.findByQuery(query);
        return new ArrayList<>(users);
    }

    public List<org.camunda.bpm.engine.identity.Group> findGroupByQueryCriteria(TeamQuery query) {
        return teamService.findByQuery(query);

    }

    public long findGroupCountByQueryCriteria(TeamQuery teamQuery) {
        return findGroupByQueryCriteria(teamQuery).size();
    }
}
