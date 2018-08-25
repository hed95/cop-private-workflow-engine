package uk.gov.homeoffice.borders.workflow.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.exception.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;
import uk.gov.homeoffice.borders.workflow.identity.Team;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TaskChecker {

    private TaskService taskService;

    public void checkUserAuthorized(@NotNull ShiftUser user, Task task) {
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());

        List<String> teams = user.getTeams().stream().map(Team::getTeamCode).collect(toList());
        List<IdentityLink> identities = identityLinks.stream().filter(i -> teams.contains(i.getGroupId())).collect(toList());

        if (identities.isEmpty() && (!user.getEmail().equalsIgnoreCase(task.getAssignee()))) {
            throw new ForbiddenException("User not authorized to action task");
        }
    }
}
