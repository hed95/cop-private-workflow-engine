package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskChecker {

    private TaskService taskService;

    public void checkUserAuthorized(User user, Task task) {
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());

        List<String> teams = Team.flatten(user.getTeam()).map(Team::getTeamCode).collect(toList());
        List<IdentityLink> identities = identityLinks.stream().filter(i -> teams.contains(i.getGroupId())).collect(toList());

        if (!user.getEmail().equalsIgnoreCase(task.getAssignee()) || identities.size() == 0) {
            throw new ForbiddenException("User not authorized to action task");
        }
    }
}
