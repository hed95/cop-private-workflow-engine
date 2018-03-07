package uk.gov.homeoffice.borders.workflow.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;
import uk.gov.homeoffice.borders.workflow.identity.Role;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.task.TaskApplicationService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@JGivenStage
public class TaskApplicationServiceStage extends Stage<TaskApplicationServiceStage> {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskApplicationService applicationService;

    private Task task;
    private Page<Task> tasks;

    public TaskApplicationServiceStage getTasksForUserIsRequested(String username) {
        Assert.notNull(task, "Task not initialised...call asTask()");
        User user = new User();
        user.setUsername(username);
        tasks = applicationService.tasks(user, new PageRequest(0, 10));
        return this;
    }

    public TaskApplicationServiceStage getTaskForCandidateGroups(String candidateGroup) {
        Assert.notNull(task, "Task not initialised...call asTask()");
        User user = new User();
        user.setUsername(UUID.randomUUID().toString());
        Role group = new Role();
        group.setName(candidateGroup);
        user.getRoles().add(group);
        tasks = applicationService.tasks(user, new PageRequest(0, 10));
        return this;
    }


    public TaskApplicationServiceStage numberOfTasksShouldBe(long numberOfTasks) {
        assertThat(tasks.getTotalElements(), is(numberOfTasks));
        return this;
    }

    public TaskApplicationServiceStage assignedTasksUsernameIs(String username) {
        List<Task> tasks = this.
                tasks.getContent()
                .stream()
                .filter(t -> t.getAssignee().
                        equalsIgnoreCase(username))
                .collect(Collectors.toList());

        assertTrue(tasks.size() >= 1);
        return this;
    }

    public TaskApplicationServiceStage aTask() {
        task = new TaskEntity(UUID.randomUUID().toString());
        task.setName("test-task");
        task.setDescription("test-description");
        return this;
    }

    public TaskApplicationServiceStage withUsername(String username) {
        Assert.notNull(task, "Task is null...please initialise with aTask()");
        task.setAssignee(username);
        return this;
    }

    public TaskApplicationServiceStage isCreated() {
        Assert.notNull(task, "Task is null...please initialise with aTask()");
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        return this;
    }

    public TaskApplicationServiceStage withCandidateGroup(String candidateGroup) {
        Assert.notNull(task, "Task is null...please initialise with aTask()");
        taskService.addCandidateGroup(task.getId(), candidateGroup);
        return this;
    }


    public TaskApplicationServiceStage numberOfPages(int numberOfPages) {
        assertThat(tasks.getTotalPages(), is(numberOfPages));
        return this;
    }

    public TaskApplicationServiceStage totalResultsIs(long totalResults) {
        assertThat(tasks.getTotalElements(), is(totalResults));
        return this;
    }
}
