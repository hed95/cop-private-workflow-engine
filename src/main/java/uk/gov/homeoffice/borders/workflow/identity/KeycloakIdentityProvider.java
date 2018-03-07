package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.*;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class KeycloakIdentityProvider implements ReadOnlyIdentityProvider {

    private UserService userService;
    private RoleService groupService;

    @Override
    public User findUserById(String userId) {
        return userService.findByUserId(userId);
    }

    @Override
    public UserQuery createUserQuery() {
        return new KeycloakUserQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        return new KeycloakUserQuery();
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }

    @Override
    public Role findGroupById(String groupId) {
        return groupService.findById(groupId);
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new KeycloakRoleQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return new KeycloakRoleQuery();
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

    }

    @Override
    public void close() {

    }

    public long findUserCountByQueryCriteria(KeycloakUserQuery query) {
        return findUserByQueryCriteria(query).size();

    }

    public List<org.camunda.bpm.engine.identity.User> findUserByQueryCriteria(KeycloakUserQuery query) {
        List<User> users = userService.allUsers();

        if (query.getId() != null) {
            users.removeIf(user -> !user.getId().equals(query.getId()));
        }
        if (query.getFirstName() != null) {
            users.removeIf(user -> !user.getFirstName().equals(query.getFirstName()));
        }
        if (query.getLastName() != null) {
            users.removeIf(user -> !user.getLastName().equals(query.getLastName()));
        }
        if (query.getEmail() != null) {
            users.removeIf(user -> !user.getEmail().equals(query.getEmail()));
        }
        if (query.getGroupId() != null) {
            users.removeIf(user -> !user.hasRole(query.getGroupId()));
        }
        return new ArrayList<>(users);
    }

    public List<org.camunda.bpm.engine.identity.Group> findGroupByQueryCriteria(KeycloakRoleQuery query) {
        return groupService.allRoles().stream()
                .filter(group -> group.getId().equals(query.getId()))
                .filter(group -> group.getName().equals(query.getName()))
                .filter(group -> group.getType().equals(query.getType()))
                .collect(Collectors.toList());
    }

    public long findGroupCountByQueryCriteria(KeycloakRoleQuery keycloakRoleQuery) {
        return findGroupByQueryCriteria(keycloakRoleQuery).size();
    }
}
